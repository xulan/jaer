/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jaer.eventio;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jaer.aemonitor.AEPacketRaw;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.chip.Chip;
import net.sf.jaer.chip.RetinaExtractor;
import net.sf.jaer.event.ApsDvsEvent;
import net.sf.jaer.event.ApsDvsEventPacket;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.event.OutputEventIterator;

/**
 * This class parses the buffer from the data files or network streams containing jAER 3.0
 * format data, as specified in http://inilabs.com/support/software/fileformat/
 * 
 * The most useful public interface for outside is the GetJaer2EventBuf(), by this method, the event
 * buffer just include addr and timestamp (like jAER 2.0 did) will be returned. After get this
 * buffer similar to jAER 2.0, all other things will be processed by AEFileInputStream. The stream
 * will be treated like it's a jAER 2.0 buffer. 
 * 
 * @author min liu
 */
public class Jaer3BufferParser {
    
    private static final Logger log = Logger.getLogger("net.sf.jaer.eventio");
    private int BUFFER_CAPACITY_BYTES = 8 * 1000000;
    private ByteBuffer in = ByteBuffer.allocate(BUFFER_CAPACITY_BYTES);
    // private ByteBuffer out = ByteBuffer.allocate(BUFFER_CAPACITY_BYTES);
    
    private final int PKT_HEADER_SIZE = 28;
    
    private PacketDescriptor currentPkt = new PacketDescriptor();
    private long numEvents = 0;
   
    /**
     * The AEChip object associated with this stream. This field was added for
     * supported jAER 3.0 format files to support translating bit locations in
     * events.
     */
    private AEChip chip = null;
    
    /**
     * Field for decoding jaer 3.0 address
     */
    public static final int JAER3YSHIFT = 2,
                JAER3YMASK = 32767 << JAER3YSHIFT, // 9 bits from bits 22 to 30
                JAER3XSHIFT = 17,
                JAER3XMASK = 32767 << JAER3XSHIFT, // 10 bits from bits 12 to 21
                JAER3POLSHIFT = 1,
                JAER3POLMASK = 1 << JAER3POLSHIFT; //,    // 1 bit at bit 11
 
    
    public enum EventType {
        SpecialEvent, PolarityEvent, FrameEvent, Imu6Event, Imu9Event, SampleEvent, EarEvent, ConfigEvent // ordered according to id code
    }

    public class PacketHeader {

        EventType eventType = EventType.SpecialEvent;
        int eventSource = 0;
        int eventSize = 0;
        int eventTSOffset = 0;
        int eventTSOverflow = 0;
        int eventCapacity = 0;
        int eventNumber = 0;
        int eventValid = 0;
    } 
   
    public class PacketDescriptor {
        
        private PacketHeader pktHeader;
        private int pktPosition;
        
        public PacketDescriptor() {           
            pktHeader  = new PacketHeader();
            pktPosition = 0;
        }
        
        public PacketHeader GetPktHeader() {
            return pktHeader;
        }
        
        public int GetPktPosition() {
            return pktPosition;
        }
        
        public void SetPktHeader(PacketHeader d) {
            pktHeader = d;
        }
        
        public void SetPosition(int position) {
            pktPosition = position;
        }
    }
    
    public class FrameDescriptor{
       int frameInfo; // see AEDAT3.0 spec for frame event
       int startOfCaptureTimestamp;
       int endOfCaptureTimestamp;
       int startOfExposureTimestamp;
       int endOfExposureTimestamp;
       int xLength; // width
       int yLength; // height
       int xPosition; // offset of LL corner in pixels
       int yPosition; // offset of LL corner in pixels
   }
       
    
    // private EventRaw tmpEventRaw = new EventRaw();
    
    private PacketDescriptor searchPacketHeader(int startPosition, int direction) throws IOException {
        PacketDescriptor pkt = new PacketDescriptor();
        PacketHeader d = pkt.GetPktHeader();
        int position = pkt.GetPktPosition();
        
        int eventTypeInt;    
        int currentPosition = in.position();  //store current position, guarntee the position didn't change in this function


        if(direction != 1 && direction != -1) {
            log.warning("Search direction can only be 1(forward) or -1(backward!)");
            return null;
        }

        in.position(startPosition);
        
     
        while((in.position() <= in.limit() - PKT_HEADER_SIZE) && in.position() >= 0) {               
            int currentSearchPosition = in.position();
            eventTypeInt = in.getShort(); 
            //  By default, eventTypeInt should range from 0 to 7
            if(eventTypeInt > 7 || eventTypeInt < 0) {
                in.position(currentSearchPosition + direction);
                continue;
            }

            d.eventType = EventType.values()[eventTypeInt];
            d.eventSource = in.getShort();
            d.eventSize = in.getInt();
            if(d.eventSize <= 0) {
                in.position(currentSearchPosition + direction);
                continue;
            }
            d.eventTSOffset = in.getInt();     //eventoverflow

            // timestamp offset can only be 6(Configuration Event), 12 (Frame Event) or 4(other events).
            if(d.eventType  == EventType.ConfigEvent) {
                if(d.eventTSOffset != 6) {
                    in.position(currentSearchPosition + direction);
                    continue;
                }
            }
            else if(d.eventType  == EventType.FrameEvent) {
                if(d.eventTSOffset != 12) {
                    in.position(currentSearchPosition + direction);
                    continue;
                }
            }
            else {
                if(d.eventTSOffset != 4) {
                    in.position(currentSearchPosition + direction);                    
                    continue;
                }
            }
            d.eventTSOverflow = in.getInt();    //eventTSOverflow
            d.eventCapacity = in.getInt();      //eventcapacity


            d.eventNumber = in.getInt();        //eventnumber
            if(d.eventNumber <= 0) {
                in.position(currentSearchPosition + direction);                
                continue;
            }
            // if(d.eventNumber != d.eventCapacity) continue;

            d.eventValid = in.getInt();         //eventValid
            if(d.eventValid < 0) {
                in.position(currentSearchPosition + direction);                
                continue;
            }

            position = in.position() - PKT_HEADER_SIZE;

            if(d.eventNumber >= d.eventValid)  break;

        }                  
            
        if(in.position() > in.limit() - PKT_HEADER_SIZE) {
            in.position(currentPosition);   //restore the position, because the byteBuffer in AEFileinputStream share the position with in.
            return null;
        }
            
        pkt.SetPktHeader(d);
        pkt.SetPosition(position);
        
        in.position(currentPosition);   //restore the position, because the byteBuffer in AEFileinputStream share the position with in.
        return pkt;
    } // searchPacketHeader
    
    private PacketDescriptor getCurrentPkt(int targetPosition) throws IOException {
        
        PacketDescriptor pkt = searchPacketHeader(targetPosition, -1);
        
        if (targetPosition - pkt.pktPosition > (pkt.pktHeader.eventSize)*(pkt.pktHeader.eventNumber) +PKT_HEADER_SIZE) {
            log.warning("Current position data is an invalid data, it doesn't belong to any packet!");
            return null;
        }
        else {
            return pkt;
        }
    }
    
    private PacketDescriptor getNextPkt(int targetPosition) throws IOException {
        return searchPacketHeader(targetPosition, 1);
    }
    
    public Jaer3BufferParser(MappedByteBuffer byteBuffer, AEChip chip) throws IOException {
        in = byteBuffer;
        in.order(ByteOrder.LITTLE_ENDIAN);    //AER3.0 spec is little endian
        this.chip = chip;
        
        chip.setEventExtractor(new jaer3EventExtrator(chip));

        currentPkt = searchPacketHeader(0, 1);
        try {
            numEvents = bufferNumEvents();
        } catch (IOException ex) {
                log.warning(ex.toString());
                Logger.getLogger(AEFileInputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * gets the size of the stream in events
     *
     * @return size in events
     */
    public long size() {
        return numEvents;
    }
    public int getCurrentEventOffset() throws IOException {
        int currentPosition = in.position();
        int nextPktPos = currentPkt.pktPosition + currentPkt.pktHeader.eventNumber * currentPkt.pktHeader.eventSize + PKT_HEADER_SIZE;
        
        // current position is not in the current packet, so we need to find the packet the current position belongs to
        if(currentPosition >= nextPktPos || currentPosition <= currentPkt.pktPosition) {   
            currentPkt = getCurrentPkt(currentPosition);
            if(currentPkt == null) {
                return -1;            // It's an invalid position, it doesn't have packet header and event data.
            }                                                                    
        }     

        int currentPktPos = currentPkt.pktPosition;
        PacketHeader packetHeader = currentPkt.pktHeader;
        // int nextPktPos = currentPktPos + packetHeader.eventNumber * packetHeader.eventSize + pkt.PKT_HEADER_SIZE;
            
        // Now we get the correct currentPktPos
        if(currentPosition - currentPktPos <= PKT_HEADER_SIZE && currentPosition - currentPktPos >=0) {  //current position is in the packet header, then the offset is the first event offset
            return currentPktPos + PKT_HEADER_SIZE;
        }
        
        // currentPktPos + PKT_HEADER_SIZE is the first event offset, so (currentPosition - (currentPktPos + PKT_HEADER_SIZE))%eventSize is the distance
        // between current position and current event offset
        int eventSize = packetHeader.eventSize;        
        return currentPosition - (currentPosition - (currentPktPos + PKT_HEADER_SIZE))%eventSize;     
    }
    
    public int getNextEventOffset() throws IOException {
        int currentEventOffset = getCurrentEventOffset();
        int currentPosition = in.position();
        
        if(-1 == currentEventOffset) {   // Current position is an invalid data or in the file end, 
            currentPkt = getNextPkt(currentPosition); 
            if(null == currentPkt) {     //It's in the file end
                return -1;
            } else {
                return currentPkt.pktPosition + PKT_HEADER_SIZE; //It's an invalid data, so we need to use the next packet
            }
        } 
        
        int currentPktPos = currentPkt.pktPosition;
        PacketHeader packetHeader = currentPkt.pktHeader;        
        int eventSize = packetHeader.eventSize;        
        int nextPktPos = currentPktPos + packetHeader.eventNumber * packetHeader.eventSize + PKT_HEADER_SIZE;        
        
        // It means it's on the event offset right now, so the next event of this position is itself
        // We can return the currentEventOffset directly, no matter it is in the middle events or the last event
        if(currentPosition == currentEventOffset) { 
            return currentEventOffset;
        }
        
        // The current position is in the last event of the current packet and not the event header, so the next event will be in the next packet.
        // We should update the currentPktPos first.
        if(currentEventOffset + eventSize >= nextPktPos) {
            currentPkt = getNextPkt(currentPosition);
            if(null == currentPkt) {     //It's in the file end
                return -1;
            } else {
                return currentPkt.pktPosition + PKT_HEADER_SIZE; //Use the next packet
            }
        }
        
        // Now we get the correct currentPktPos and the current position is in the header.
        if(currentPosition - currentPktPos <= PKT_HEADER_SIZE && currentPosition - currentPktPos >=0) {  //current position is in the packet header, then the next offset is the first event offset
            return currentPktPos + PKT_HEADER_SIZE;
        }
        
        return currentEventOffset + eventSize;
    } //GetNumEventOffset
    
    public int getLastTimeStamp() throws IOException {
        int currentPosition = in.position();
        PacketDescriptor pkt = getCurrentPkt(in.limit() - PKT_HEADER_SIZE);
        int lastTs;
        if(pkt.pktHeader.eventNumber == pkt.pktHeader.eventValid) {
            int position = pkt.pktPosition + PKT_HEADER_SIZE + (pkt.pktHeader.eventNumber - 1) * pkt.pktHeader.eventSize + pkt.pktHeader.eventTSOffset;
            in.position(position);
            lastTs =  in.getInt();
        } else {
            ByteBuffer tmpBuffer = ByteBuffer.allocate(8);
            in.position(pkt.pktPosition);
            for(int i = 0; i < pkt.pktHeader.eventValid; i++) {
                tmpBuffer = getJaer2EventBuf();
            }
            tmpBuffer.getInt();   //addr
            lastTs = tmpBuffer.getInt();
        }
        
        in.position(currentPosition);   //Restore last position
        return lastTs;        
    }
    
    public ByteBuffer getJaer2EventBuf() throws IOException {
        int nextEventOffset = getNextEventOffset();
        if(-1 == nextEventOffset) {
            log.warning("Reach the end of file, can't read data!");
            return null;
        }

        final int validMask = 0x00000001;
        int eventFirstInt;
        in.position(nextEventOffset);        
        eventFirstInt = in.getInt();        
        
        // This while loop is used to exclude the invalid events and non-polarity packets
        while((eventFirstInt & validMask) != 1 || (currentPkt.pktHeader.eventType != EventType.PolarityEvent)) {
            try {
                if(currentPkt.pktHeader.eventType != EventType.PolarityEvent) {
                    in.position(currentPkt.pktPosition + currentPkt.pktHeader.eventNumber * currentPkt.pktHeader.eventSize + PKT_HEADER_SIZE);
                }                
            } catch(IllegalArgumentException e) {
                log.log(Level.INFO, "Packet Position is {0} and current position is {1}", new Object[]{currentPkt.pktPosition, in.position()});
            }

            nextEventOffset = getNextEventOffset();
            in.position(nextEventOffset);        
            eventFirstInt = in.getInt();              
        }       

        
        int addrOffset = GetAddrOffset(currentPkt.pktHeader);
        int tsOffset = currentPkt.pktHeader.eventTSOffset;
        ByteBuffer jaer2Buffer = ByteBuffer.allocate(8);
        
        in.position(nextEventOffset + addrOffset);
        int addr =  in.getInt();
        // addr = this.extractJaer2Addr(addr);
        in.position(nextEventOffset + tsOffset);
        int ts = in.getInt();
        
        jaer2Buffer.putInt(addr);
        jaer2Buffer.putInt(ts);
        jaer2Buffer.flip();
        return jaer2Buffer;
    }
        
    public long bufferNumEvents() throws IOException {
        PacketDescriptor pkt = getNextPkt(0);
        long numEvents = 0;
        while(pkt != null) {
            if(pkt.pktHeader.eventType == EventType.PolarityEvent) {
                numEvents += pkt.pktHeader.eventValid;                
            }
            pkt = getNextPkt(pkt.pktPosition + 1);
        }
        return numEvents;
    }
    

    
    private int GetAddrOffset(PacketHeader packetHeader) throws IOException {
        int eventAddrOffset = 0;
        
        switch (packetHeader.eventType) {
                case PolarityEvent:
                    eventAddrOffset = 0;
                    break;
                    //readNextPacket();                    ;
                case FrameEvent:
                    eventAddrOffset = 0;
                    // readFrame(packetHeader);
                    break;
                case SampleEvent:
                    eventAddrOffset = 0;
                    break;
                case ConfigEvent:
                    eventAddrOffset = 0;
                    break;
                case Imu6Event:
                    eventAddrOffset = 0;
                    break;
                case Imu9Event:
                    eventAddrOffset = 0;
                    break;
                default: eventAddrOffset = 0;
            }
 
        return eventAddrOffset;
    }       
     
    public int getCurrentPktPos() {
        return currentPkt.pktPosition;
    }
    
    public PacketHeader getPacketHeader() {
        return currentPkt.pktHeader;
    }

    public void setInBuffer(ByteBuffer BufferToBeProcessed) {
        in = BufferToBeProcessed; //To change body of generated methods, choose Tools | Templates.
    }
    
    public void setInBufferOrder(ByteOrder order) {
        in.order(order);
    }   
    
    
    public class jaer3EventExtrator extends RetinaExtractor {
                		
        public jaer3EventExtrator(final AEChip chip) {
            super(chip);
        }
        
        /**
        * extracts the meaning of the raw events.
        *
        * @param in
        *            the raw events, can be null
        * @return out the processed events. these are partially processed
        *         in-place. empty packet is returned if null is supplied as in.
        */
        @Override
        synchronized public EventPacket extractPacket(final AEPacketRaw in) {
            if (out == null) {
                out = new ApsDvsEventPacket(chip.getEventClass());
            }
            else {
                    out.clear();
            }
            out.setRawPacket(in);
            if (in == null) {
                    return out;
            }
            final int n = in.getNumEvents(); // addresses.length;
            final int sx1 = chip.getSizeX() - 1;
            chip.getSizeY();

            final int[] datas = in.getAddresses();
            final int[] timestamps = in.getTimestamps();
            final OutputEventIterator outItr = out.outputIterator();
            final PacketHeader pktHeader = Jaer3BufferParser.this.currentPkt.pktHeader;
            // NOTE we must make sure we write ApsDvsEvents when we want them, not reuse the IMUSamples

            // at this point the raw data from the USB IN packet has already been digested to extract timestamps,
            // including timestamp wrap events and timestamp resets.
            // The datas array holds the data, which consists of a mixture of AEs and ADC values.
            // Here we extract the datas and leave the timestamps alone.
            // System.out.println("Extracting new packet "+out);
                for (int i = 0; i < n; i++) { 
                        // events and still delivering frames
                        final int data = datas[i];
                        
                        switch (pktHeader.eventType) {
                            case PolarityEvent:
                                readDVS(outItr, data, timestamps[i]);
                                break;
                            case FrameEvent:
                                // readFrame(packetHeader);
                                break;
                            case SampleEvent:
                                // readSample();
                                break;
                            case ConfigEvent:
                                // readConfig();
                                break;
                            case Imu6Event:
                                // readImu6();
                                break;
                            case Imu9Event:
                                // readImu9();;
                                break;
                            default: 
                        }
                    /*    
                    if ((getAutoshotThresholdEvents() > 0) && (autoshotEventsSinceLastShot > getAutoshotThresholdEvents())) {
                            takeSnapshot();
                            autoshotEventsSinceLastShot = 0;            
                    }     
                    */              
            }     
            return out;    
        }// extractPacket
        
        // TODO hack to reuse IMUSample events as ApsDvsEvents holding only APS or DVS data by using the special flags
        protected ApsDvsEvent nextApsDvsEvent(final OutputEventIterator outItr) {
                final ApsDvsEvent e = (ApsDvsEvent) outItr.nextOutput();
                e.special = false;
                e.adcSample = -1;
                // if (e instanceof IMUSample) {
                        // ((IMUSample) e).imuSampleEvent = false;
                // }
                return e;
        }
    
        private void readDVS(final OutputEventIterator outItr, final int data, final int timestamp) {
            final int sx1 = chip.getSizeX() - 1;            
            final ApsDvsEvent e = nextApsDvsEvent(outItr);

            e.adcSample = -1; // TODO hack to mark as not an ADC sample
            e.special = false;
            e.address = data;
            e.timestamp = timestamp;
            e.polarity = ((data & JAER3POLMASK)  >> JAER3POLSHIFT) == (JAER3POLMASK >> JAER3POLSHIFT) ? ApsDvsEvent.Polarity.On : ApsDvsEvent.Polarity.Off;
            e.type = 0;
            e.x = (short) (sx1 - ((data & JAER3XMASK) >>> JAER3XSHIFT));
            e.y = (short) ((data & JAER3YMASK) >>> JAER3YSHIFT);

            e.setIsDVS(true);     
        }
    }
  
}