import java.net.DatagramPacket;

public class Packet {
    private DatagramPacket dp;
    private int tipo;
    private int id;
    private int n_part;
    private int checksum;
    private int offset;
    private String ficheiro;

    public Packet(int tipo, int id, int n_part, byte [] dados, int tamanho, int checksum, int offset, String ficheiro){
        this.tipo = tipo;
        this.id = id;
        this.n_part = n_part;
        this.dp = new DatagramPacket(dados, tamanho);
        this.checksum = checksum;
        this.ficheiro = ficheiro;
    }



}
