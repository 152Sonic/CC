import java.net.DatagramPacket;

public class Packet {
    private DatagramPacket dp;
    private int tipo;
    private int id;
    private int n_part;
    private int checksum;

    Packet(int tipo, int id, int n_part, byte [] dados, int tamanho, int checksum, int offset){
        this.tipo = tipo;
        this.id = id;
        this.n_part = n_part;
        this.dp = new DatagramPacket(dados, offset, tamanho);
        this.checksum = checksum;
    }



}
