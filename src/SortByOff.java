import java.util.Comparator;

public class SortByOff implements Comparator<Packet> {


    public int compare(Packet packet, Packet t1) {
        return packet.getOff() - t1.getOff();
    }
}
