//Va dentro del servidor

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

//La clase servidor contiene una carpeta con muchos objetos que contienen los datos del Usuario

class Servidor2{

     File usuariosCarpeta;

     public Servidor2(){
         MarcoServidor mimarco = new MarcoServidor();
         mimarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         usuariosCarpeta = new File("carpetaUsuarios");
         usuariosCarpeta.mkdir();
         try{
         //Se crea un servidor para determinar cual ventana se abrio si es de Ingreso/Registro/Inicio
         ServerSocket s = new ServerSocket(9898);

         while(true){
            Socket ss = s.accept();
            DataInputStream ent = new DataInputStream(ss.getInputStream());
            String tipVentana = ent.readUTF();
            ent.close();

            if(tipVentana.equals("1")){
               System.out.println("Es de registro");
               HiloRegistros miHilo = new HiloRegistros(s);
               miHilo.start();

            }else{
               System.out.println("Es de ingreso");
               HiloIngreso miHilo2 = new HiloIngreso(s);
               miHilo2.start();
             }

          }

          }catch(Exception e){}
    }

     public static void main(String [] args){

         Servidor2 servidor = new Servidor2();

     }

     class MarcoServidor extends JFrame{

     public MarcoServidor(){

                      setBounds(800,300,400,200);
                      JPanel milamina = new JPanel();
                      milamina.setLayout(new BorderLayout());
                      areatexto = new JTextArea();
                      milamina.add(areatexto, BorderLayout.CENTER);
                      add(milamina);
                      setVisible(true);
          }

       }
     JTextArea areatexto ;
}

//Clase thread que valida el ingreso de un usurio

class HiloIngreso extends Thread{

  ServerSocket s;

  HiloIngreso(ServerSocket s){
    this.s = s;
  }

  public void run(){
    try{
      String resp;

      while(true){

          Socket socketIn = s.accept();
          ObjectInputStream flujo_en = new ObjectInputStream(socketIn.getInputStream());
          UsuarioServidor datoIngreso = (UsuarioServidor) flujo_en.readObject();
          System.out.println(datoIngreso.getUser()+" "+datoIngreso.getPass());
          File dir = new File("carpetaUsuarios");
          String [] ficheros = dir.list();
          int error = 0;
          if(ficheros != null){
             for(String user : ficheros){
               System.out.println(user);
                    if(!user.equals(".DS_Store")){
                       BufferedReader br = new BufferedReader(new FileReader("carpetaUsuarios/"+user+"/info.txt"));
                       if(user.equals(datoIngreso.getUser().trim()) && br.readLine().trim().equals(datoIngreso.getPass().trim())){
                          error=error+1;
                       }
                    }
              }
          }
          if(error==1){
               resp = "1";
          }else{
               resp = "-2";
          }
          System.out.println(resp);
          InetAddress localizar = socketIn.getInetAddress();
          String ip = localizar.getHostAddress();
          Socket enviaUsuario = new Socket(ip,6450);
          DataOutputStream res = new DataOutputStream(enviaUsuario.getOutputStream());
          if(resp.equals("1")){
                  /*File userFiles = new File("carpetaUsuarios"+"/"+user);
                  String [] archivos = userFiles.list();
                  for(int i ; i < archivos.length(); i++){
                      if(archivos[i].equals(info.txt)){
                         archivos[i] = archivos[i+1];
                      }
                  }
                  ObjectOutputStream envia_archivos = new ObjectOutputStream(enviaUsuario.getOutputStream());
                  envia_archivos.writeObject(archivos);
                  envia_archivos.close();*/
                  res.writeUTF(resp);
                  System.exit(0);
              }else{
                res.writeUTF(resp);
                res.close();
                enviaUsuario.close();
              }
          }
      }catch(IOException | ClassNotFoundException ioe){
           ioe.printStackTrace();
      }
      catch(Exception e){
           e.printStackTrace();
      }
    }
}



class HiloRegistros extends Thread{
     ServerSocket s;

     HiloRegistros(ServerSocket s){
       this.s = s;
     }

     public void run(){
           try{
             Datos2 datosUsuario;
             while(true){

                 Socket miSocket = s.accept();

                 //Primera conexion, se recibe un Objeto de tipo UsuarioServidor

                 ObjectInputStream flujo_entrada = new ObjectInputStream(miSocket.getInputStream());
                 UsuarioServidor dato = (UsuarioServidor) flujo_entrada.readObject();
                 System.out.println(dato.getUser()+" "+dato.getEmail()+" "+dato.getPass());
                 datosUsuario = new Datos2(dato.getUser(),dato.getEmail(),dato.getPass());
                 String resp = datosUsuario.validarIngreso();
                 System.out.println(resp);

                 // Ya validando los datos de ingreso se va a devolver un String al usuario para que responda

                 InetAddress localizar = miSocket.getInetAddress();
                 String ip = localizar.getHostAddress();
                 Socket enviaUsuario = new Socket(ip,6450);
                 DataOutputStream res = new DataOutputStream(enviaUsuario.getOutputStream());

                 if(resp.equals("1")){
                     File usuario = new File("carpetaUsuarios/"+dato.getUser());
                     if(usuario.mkdir()){
                         FileWriter ingresarInfo = new FileWriter("carpetaUsuarios/"+dato.getUser()+"/info.txt");
                         PrintWriter salida = new PrintWriter(new BufferedWriter(ingresarInfo));
                         salida.println(dato.getPass());
                         salida.close();
                         res.writeUTF(resp);
                     }
                   }
                  else{
                    res.writeUTF(resp);
                  }
                  res.close();
                  miSocket.close();
                }
             }catch(IOException | ClassNotFoundException ioe){
                  ioe.printStackTrace();
             }
             catch(Exception e){
                  e.printStackTrace();
             }
     }
}

//Clase que valida los datos que ingreso el usuario

class Datos2{
    String usuario;
    String email;
    String clave;

    public Datos2(){}

    public Datos2(String usuario,String mail, String clave){
        this.usuario = usuario;
        this.email = mail;
        this.clave = clave;
    }

    public String validarIngreso(){
        if(usuario.length() == 8 && clave.length() == 8){
            int num=0,num2=0,j,k;
            for(int i=0;i<8;i++){
                j=this.usuario.charAt(i);
                k=this.clave.charAt(i);
                if((j==45||j==46||j==95)||(j>=48&&j<=57)||(j>=64&&j<=90)||(j>=97&&j<=122)){
                    num+=1;
                }
                if((k==45||k==46||k==95)||(k>=48&&k<=57)||(k>=64&&k<=90)||(k>=97&&k<=122)){
                    num2+=1;
                }
            }
            if(num==8 && num2==8){
                try{
                    File dir = new File("carpetaUsuarios");
                    String [] ficheros = dir.list();
                    int error = 0;
                    if(ficheros != null){
                        for(String user : ficheros){
                          System.out.println(user);
                          if(user.equals(this.usuario.trim())){
                             error=error+1;
                          }
                      }
                    }
                    if(error==0){
                         return "1";
                      }else{
                         return "-1";
                      }
                }catch(Exception ioe){
                    ioe.printStackTrace();
                    System.out.println("Error");
                }
            }else{
                return "-2";
            }
        }else{
             return "-3";
        }
        return "1";
    }
}
