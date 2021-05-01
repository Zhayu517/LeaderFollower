import comp34120.ex2.PlayerImpl;
import comp34120.ex2.PlayerType;
import comp34120.ex2.Record;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class MK3MLLeader extends PlayerImpl {

    protected MK3MLLeader() throws RemoteException, NotBoundException {
        super(PlayerType.LEADER, "MK3 ML Leader");
    }

//    private String getAllPublishedPriceAsInput(int end_date) throws RemoteException {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < end_date; i++) {
//            Record record = m_platformStub.query(m_type, end_date);
//            sb
//                    .append(record.m_leaderPrice)
//                    .append(" ")
//                    .append(record.m_followerPrice)
//                    .append(" ");
//        }
//        return sb.toString();
//    }

    private float runPyScript(String script, String input) throws IOException {
        Process process = Runtime.getRuntime().exec("py " + script + " " + input.trim());
        Scanner scanner = new Scanner(process.getInputStream()).useDelimiter("\\A");
        if (!scanner.hasNext()) {
            Scanner s = new Scanner(process.getErrorStream()).useDelimiter("\\A");
            System.err.println("Error output");
            System.err.println(s.hasNext() ? s.next() : "");
            throw new IllegalStateException("py script from " + script + " did not receive any output");
        }
        return Float.parseFloat(scanner.next().trim());
    }

    @Override
    public void proceedNewDay(int p_date) throws RemoteException {
        try {
            float price = runPyScript("ml_mk3_leader.py", Integer.toString(p_date));
            m_platformStub.publishPrice(m_type, price);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to execute py script", e);
        }
    }

    @Override
    public void goodbye() throws RemoteException {
        MK3MLLeader.ExitTask.exit(500);
    }

    private static class ExitTask extends TimerTask {
        static void exit(final long p_delay) {
            (new Timer()).schedule(new ExitTask(), p_delay);
        }

        @Override
        public void run() {
            System.exit(0);
        }
    }

    public static void main(final String[] p_args) throws RemoteException, NotBoundException {
        new MK3MLLeader();
    }
}
