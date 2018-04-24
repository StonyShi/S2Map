
/**
 * <p>S2-Map
 * <p>PACKAGE_NAME
 *
 * @author stony
 * @version 下午1:35
 * @since 2018/4/13
 */
public class MainServer {

    public static void main(String[] args){
        try {
            com.stony.map.NettyServer.main(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}