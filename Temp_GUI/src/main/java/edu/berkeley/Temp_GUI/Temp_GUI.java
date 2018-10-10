package edu.berkeley.Temp_GUI;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.DecimalFormat;
import java.util.Arrays;

import javax.swing.SwingWorker;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;



/**
 *
 * @author Ben
 */
public class Temp_GUI extends javax.swing.JFrame {
	
	//Packet structure is: byte(0) STARTBYTE -> byte(1) packet length -> byte(2) checksum -> byte(3) packet identifier -> byte(4-n) data packet;
	private SerialPort arduinoPort; //Port object for communication to the Arduino via JSerialComm
    private SerialPort[] serialPorts; //Array of COM port objects that are currently open
    private byte[] readBuffer = new byte[1024]; //Array for storing the read buffer that can contain at least one packet (max size 256 bytes);
    private int readLength = 0; //Number of bytes in most recent load of rx serial buffer
    private boolean arduinoConnect = false; //Whether the GUI if currently connected to a driver
    private int nArduino = 0; //Number of Arduino devices found connected to computer
	private int BAUDRATE = 250000; //Baud rate for serial connection
	private double WARNTEMP = 50; //Temperature at which driver and GUI will warn user of overheat state - 0-input, 1-output, 2-external
	private double FAULTTEMP = 70; //Temperature at which driver will shutoff automatically - 0-input, 1-output, 2-external
	private static final DecimalFormat df1 = new DecimalFormat("##.#");
	/**
     * Creates new form Temp_GUI
     */
    public Temp_GUI() {
        initComponents();
        initSelfListeners();
        this.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel2.setBackground(new java.awt.Color(200, 200, 200));
        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 48)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Not Connected");
        jLabel2.setOpaque(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>                        

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Temp_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Temp_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Temp_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Temp_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Temp_GUI().setVisible(true);
            }
        });
    }
    
  //Add frame listener for window opening, closing, etc. events
    void initSelfListeners(WindowListener taskStarterWindowListener) {
    	this.addWindowListener(taskStarterWindowListener);   	
    }
    
    //This method is used to avoid calling an overridable method ('addWindowListener()') from within the constructor.
    //Code is from: https://stackoverflow.com/questions/39565472/how-to-automatically-execute-a-task-after-jframe-is-displayed-from-within-it
    private void initSelfListeners() {
        WindowListener taskStarterWindowListener = new WindowListener() {
            public void windowOpened(WindowEvent e) {
System.out.println("Starting serial..."); //Perform task here. In this case, we are simulating a startup (only once) time-consuming task that would use a worker.
        	    //Perform handshaking on background thread so as not to lock-up the GUI
				SwingWorker<Integer, Integer> StartupLoader = new SwingWorker<Integer, Integer>() {
	       	        protected Integer doInBackground() throws Exception {
						initializeSerial();
	      	            return 100;
	       	        }
				};
				StartupLoader.execute();
            }
            public void windowClosing(WindowEvent e) {
            	if(arduinoPort != null) {
	            	arduinoPort.writeBytes(new byte[] {0}, 1); //Send command to stop streaming temperature data
	            	arduinoPort.closePort();
            	}
           }
            public void windowClosed(WindowEvent e) {
                //Do nothing...Or drink coffee...NVM; always drink coffee!
            }
           public void windowIconified(WindowEvent e) {
                //Do nothing...Or do EVERYTHING!
            }

           public void windowDeiconified(WindowEvent e) {
                //Do nothing...Or break the law...
            }

            public void windowActivated(WindowEvent e) {
                //Do nothing...Procrastinate like me!
            }

            public void windowDeactivated(WindowEvent e) {
                //Do nothing...And please don't notice I have way too much free time today...
            }
        };

        //Here is where the magic happens! We make (a listener within) the frame start listening to the frame's own events!
       this.addWindowListener(taskStarterWindowListener);
   }
    
    private boolean initializeSerial(){
        //Generate an array of available ports on system
        //Make instance of GUI
        int nPorts = SerialPort.getCommPorts().length;
        serialPorts = SerialPort.getCommPorts();
        jLabel2.setText("Connecting...");
        int a;
        
       for(a = 0; a < nPorts; a++){
            arduinoPort = serialPorts[a];
System.out.println("Testing " +  arduinoPort.getDescriptivePortName());
            arduinoPort.setBaudRate(BAUDRATE);
            arduinoPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 2000, 2000); //Blocking means wait the full 2000ms to catch the set number of bytes
            arduinoPort.openPort();
            if(handShake()) {
            	a = nPorts + 1;
                arduinoPort.addDataListener(new SerialPortDataListener() {
            	   public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
            	   public void serialEvent(SerialPortEvent event)
            	   {
					checkTemp();
            	   }
                });
            }
            else {
            	arduinoPort.closePort();	
            }
       }
       if(a == nPorts) {
    	   jLabel2.setText("No Device Found");
       }
      
        //Inform user if no devices were found
//        nArduino = data.getNarduino();
 //       if(nPorts == 0) gui.updateProgress(0, "No available COM ports found on this computer.");
 //       else if(nArduino == 0) gui.updateProgress(0, "Arduino not found.");
 //       else if(nArduino == 1) gui.updateProgress(0, "Disconnected: " + nArduino + " device found.");
 //       else gui.updateProgress(0, "Disconnected: " + nArduino + " devices available.");
        
 //       data.initializeFinished(); //Inform the model that the initialization is complete
        return true; //Inform the GUI that the initialization is complete
    }
    private boolean handShake() {
    	boolean connect = false;
    	try { //Wait for arduino to boot
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	arduinoPort.writeBytes(new byte[] {0}, 1); //Send command triggering arduino to reply with ID code
    	readLength = arduinoPort.readBytes(readBuffer, readBuffer.length);
    	
    	for (int a = 0; a<readLength; a++) {
    		if ((readBuffer[a] & 0xFF) == 0 && (readBuffer[a+1] & 0xFF) == 179 && (readBuffer[a+2] & 0xFF) == 162 && (readBuffer[a+3] & 0xFF) == 110 && (readBuffer[a+4] & 0xFF) == 72) {
    			connect = true;
    			arduinoPort.writeBytes(new byte[] {0}, 1); //Send command triggering arduino to start streaming temperature data
    		}
    	}
    	return connect;
    }
    
    private void checkTemp(){
        readLength = arduinoPort.readBytes(readBuffer, readBuffer.length);
System.out.println("Buffer: " + Arrays.toString(readBuffer));

		for (int a = 0; a<readLength; a++) {
			double temp = ADCtoCelcius(readBuffer[a]);
			jLabel2.setText(df1.format(temp) + "°C");
			if (temp>FAULTTEMP) {
				jLabel2.setBackground(new java.awt.Color(255, 0, 0));
			}
			else if(temp>WARNTEMP) {
				jLabel2.setBackground(new java.awt.Color(255, 255, 0));
			}
			else {
				jLabel2.setBackground(new java.awt.Color(0, 255, 0));
			}
		}
        //If minimal packet size is received then verify contents

    }
    
    private double ADCtoCelcius(byte adcByte) {
    	double SERIESR = 4700;
    	double Ro = 10000;
    	double beta = 3434;
    	double To = 25;
    	double adcDouble = (double) (adcByte & 0xFF); //Convert unsigned byte to double
    	
    	//Math from: https://learn.adafruit.com/thermistor/using-a-thermistor
    	double conversion = (-1*SERIESR*adcDouble) / (adcDouble-255D);
    	conversion = conversion/Ro;
    	conversion = Math.log(conversion);
    	conversion /= beta;
    	conversion += 1D/(To+273.15D);
    	conversion = 1D/conversion;
    	conversion -= 273.15D;

     	return conversion;
    }
    
    private void updateLabel(String string) {
    	jLabel2.setText(string);
    }
    
    private void updateColor(int R, int G, int B) {
    	jLabel2.setBackground(new java.awt.Color(R, G, B));
    }

    // Variables declaration - do not modify                     
    private javax.swing.JLabel jLabel2;
    // End of variables declaration                   
}