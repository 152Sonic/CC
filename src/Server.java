import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {
    private int porta;
    private String ip;
    private int estado; //0 -> livre; 1 -> ocupado

    public Server (int porta, String ip, int estado){
        this.porta = porta;
        this.ip = ip;
        this.estado = estado;
    }

    public int getEstado(){
        return this.estado;
    }

    public int getPorta() {
        return this.porta;
    }

    public String getIp() {
        return this.ip;
    }

    public InetAddress getInetAddress() throws UnknownHostException{
        return InetAddress.getByName(this.ip);
    }
}
