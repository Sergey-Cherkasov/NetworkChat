package br.svcdev.client;

import br.svcdev.client.controller.ClientController;

import java.io.IOException;

public class ClientApplication {

   private static final String HOST_NAME = "localhost";
   private static final int PORT_TO_CONNECT = 82_83;

   public static void main(String[] args) {

      try {
         ClientController clientController = new ClientController(HOST_NAME, PORT_TO_CONNECT);
         clientController.initConnection();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

}
