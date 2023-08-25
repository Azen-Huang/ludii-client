import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import game.functions.ints.state.Var;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.move.Move;
import utils.AIUtils;

import java.io.*;
import java.lang.ProcessBuilder.Redirect.Type;
import java.net.*;

public class StoneAI extends AI
{

    //-------------------------------------------------------------------------

    /** Our player index */
    protected int player = -1;
    protected double value = 0;
    protected String analysisReport = null;

    //-------------------------------------------------------------------------

    /**
     * Constructor
     */
    public StoneAI()
    {
        this.friendlyName = "Stone";
    }

    //-------------------------------------------------------------------------

    @Override
    public Move selectAction
    (
        final Game game, 
        final Context context, 
        final double maxSeconds,
        final int maxIterations,
        final int maxDepth
    )
    {
        FastArrayList<Move> legalMoves = game.moves(context).moves();
        List<Integer> board = new ArrayList<Integer>();
        ArrayList<Integer> history = new ArrayList<Integer>();
        int i;
        for(i=0;i<context.trial().moveNumber();i++){
            history.add(context.trial().getMove(i).to());
        }

        for(i=0;i<225;i++){
            board.add(0);
        }
        if(player==1){
            for(i=0;i<context.trial().moveNumber();i++){
                if(i%2==0){
                    board.set(context.trial().getMove(i).to(), 1);
                }else{
                    board.set(context.trial().getMove(i).to(), -1);
                }
            }
        }else{
            for(i=0;i<context.trial().moveNumber();i++){
                if(i%2==0){
                    board.set(context.trial().getMove(i).to(), -1);
                }else{
                    board.set(context.trial().getMove(i).to(), 1);
                }
            }
        }

        // If we're playing a simultaneous-move game, some of the legal moves may be 
        // for different players. Extract only the ones that we can choose.
        if (!game.isAlternatingMoveGame())
            legalMoves = AIUtils.extractMovesForMover(legalMoves, player);
        
        String send_string = history.toString();
        
        String send_string_1 = "n ";
        for(i=0;i<225;i++){
            if(board.get(i)==0){
                send_string_1 += "0 ";
            }else if(board.get(i)==1){
                send_string_1 += "1 ";
            }else if(board.get(i)==-1){
                send_string_1 += "-1 ";
            }
        }

        int r = 0, done = 0;
        
        // while(done==0){
        //     try {
        //         Socket clientSocket = new Socket();
        //         clientSocket.connect(new InetSocketAddress("140.122.184.89", 7000), 1000);
        //         PrintStream out = new PrintStream(clientSocket.getOutputStream());  
        //         System.out.println("Send.");
        //         out.println(send_string_1);
        //         System.out.println("Wait.");
        //         BufferedReader buf =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        //         String echo = buf.readLine();
        //         String[] split = echo.split(" ");
        //         r = Integer.parseInt(split[0]);
        //         if(r>=0){
        //             value = 0.0;
        //             analysisReport = "Open";
        //             clientSocket.close();
        //             done = 1;
        //         }else{
        //             clientSocket.close();
        //             break;
        //         }
        //     } catch (IOException e) {
        //         // TODO Auto-generated catch block
        //         System.out.println("error");
        //         e.printStackTrace();
        //     }
        // }
        System.out.println("Start.");
        while(done==0){
            try {
                Socket clientSocket = new Socket();
                clientSocket.connect(new InetSocketAddress("140.122.184.66", 7000), 1000);
                PrintStream out = new PrintStream(clientSocket.getOutputStream());  
                System.out.println("Send.");
                out.println(send_string);
                System.out.println("Wait.");
                BufferedReader buf =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String echo = buf.readLine();
                String[] split = echo.split(" ");
                double max_pi = -99999;
                System.out.println(echo);
    
                for(i=0;i<225;i++){
                    if(Double.parseDouble(split[i])>max_pi){
                        max_pi = Double.parseDouble(split[i]);
                        r = i;
                    }
                }
                value = Double.parseDouble(split[225])/5000.0-1.0;
                analysisReport = "Win Rate: "+Double.toString((Double.parseDouble(split[225]) + 1.0) * 50.0)+"%";
                clientSocket.close();
                done = 1;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println("error");
                e.printStackTrace();
            }
        }

        for(i=0;i<legalMoves.size();i++){
            if(legalMoves.get(i).to()==r){
                board.set(r, 1);
                r = i;
                break;
            }
        }
        return legalMoves.get(r);
    }

    @Override
    public void initAI(final Game game, final int playerID)
    {
        this.player = playerID;
    }

    @Override
    public double estimateValue()
	{
		return value;
	}

    @Override
	public String generateAnalysisReport()
	{
		return analysisReport;
	}

    //-------------------------------------------------------------------------

}