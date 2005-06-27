/* $Id$ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import games.stendhal.client.*;
import games.stendhal.common.*;

import java.net.*;
import java.io.*;

import marauroa.client.*;
import marauroa.client.net.*;
import marauroa.common.*;
import marauroa.common.net.*;
import marauroa.common.game.*;

import javax.swing.*;

/** The main class that create the screen and starts the arianne client. */
public class j2DClient extends JFrame
  {
  private GameScreen screen;
  private InGameGUI inGameGUI;

  private boolean gameRunning=true;

  private StendhalClient client;

  private String host;
  private String username;
  private String password;

  /** NOTE: It sounds bad to see here a GUI component. Try other way. */
  private JTextField playerChatText;

  private static String[] nextString(String from) {
    char[] cFrom = from.toCharArray();
    String[] res = new String[2];
    res[0] = "";
    res[1] = "";
    int quote = 0;
    char sep = ' ';
    int i=0;
    if(cFrom[0] == '\'') {
      quote = 1;
    }
    if(cFrom[0] == '"') {
      quote = 2;
    }
    if(quote != 0) {
      i++;
      sep = cFrom[0];
    }
    for(;i<cFrom.length;i++) {
      switch(quote) {
        case 0:
        case 1:
          if(cFrom[i] == sep) {
              res[0] = from.substring(i+1);
              return res;
          }
          res[1] += cFrom[i];
          break;
        case 2:
          if(cFrom[i]=='"')
            {
            res[0] = from.substring(i+1);
            return res;
            }
          else
            {
            i++;
            if(i==cFrom.length)
              {
              return null;
              }
            
            res[1] += cFrom[i];
            }
          break;
      }
    }
    if(quote == 0) {
      return res;
    }
    return null;
  }

  private static String[] parseString(String s, int nbPart)
    {
    String[] res = new String[nbPart];
    String [] t;
    int i;
    s = s.trim();
    for(i=0;i<nbPart - 1;i++)
      {
      t = nextString(s);
      if(t == null) {
        return null;
      }
      res[i] = t[1];
      s = t[0].trim();
      }
    res[i] = s;
    return res;
    }

  public j2DClient(StendhalClient sc)
    {
    super();

    // create a frame to contain our game
    setTitle("Stendhal "+stendhal.VERSION+" - a multiplayer online game using Arianne");

    URL url = this.getClass().getClassLoader().getResource("data/StendhalIcon.gif");
    this.setIconImage(new ImageIcon(url).getImage());

    // get hold the content of the frame and set up the resolution of the game
    JPanel panel = (JPanel) this.getContentPane();
    panel.setPreferredSize(new Dimension(640,480));
    panel.setLayout(null);

    // setup our canvas size and put it into the content of the frame
    Canvas canvas=new Canvas();
    canvas.setBounds(0,0,640,460);
      // Tell AWT not to bother repainting our canvas since we're
      // going to do that our self in accelerated mode
      canvas.setIgnoreRepaint(true);
      panel.add(canvas);

      playerChatText=new JTextField("");
      playerChatText.setBounds(0,460,640,20);
    playerChatText.addActionListener(new ActionListener()
      {
      // TODO: Take this out of here. Place on a proper (hidden) class.
      // TODO: ...inGameGUI integration...
      public void actionPerformed(ActionEvent e)
        {
      	String text = playerChatText.getText();
      	text.trim();

        if(text.startsWith("/tell ") ||text.startsWith("/msg ")) // Tell command
      	  {
      	  String[] command = parseString(text, 3);
      	  if(command != null)
      	    {
      	    RPAction tell = new RPAction();
      	    tell.put("type","tell");
      	    tell.put("who", command[1]);
      	    tell.put("text", command[2]);
      	    client.send(tell);
      	    }
      	  }
        else if(text.startsWith("/where ")) // Tell command
          {
          String[] command = parseString(text, 2);
          if(command != null)
            {
            RPAction tell = new RPAction();
            tell.put("type","where");
            tell.put("who", command[1]);
            client.send(tell);
            }
          }
        else if(text.equals("/who")) // Who command
      	  {
      	  RPAction who = new RPAction();
      	  who.put("type","who");
      	  client.send(who);
      	  }
      	else if(text.startsWith("/improve ")) //Improve command
      	  {
      	  System.out.println ("Improving");
      	  String[] command = parseString(text, 2);
      	  if(command != null)
      	    {
            System.out.println ("Improving "+command[1]);
            RPAction improve = new RPAction();
      	    improve.put("type","improve");
      	    improve.put("stat", command[1]);
      	    client.send(improve);
      	    }
      	  }
      	else // Chat command
      	  {
          RPAction chat=new RPAction();
          chat.put("type","chat");
          chat.put("text",playerChatText.getText());
          client.send(chat);
      	  }

        playerChatText.setText("");
        }
      });
    panel.add(playerChatText);


    this.setLocation(new Point(100, 100));
    this.setIconImage(new ImageIcon("data/StendhalIcon.gif").getImage());

      // finally make the window visible
    pack();
    setResizable(false);
    setVisible(true);

    // add a listener to respond to the user closing the window. If they
    // do we'd like to exit the game
    addWindowListener(new WindowAdapter()
      {
      public void windowClosing(WindowEvent e)
        {
        gameRunning=false;
        }
      });

    this.client=sc;

    canvas.addFocusListener(new FocusListener()
      {
      public void focusGained(FocusEvent e)
        {
        playerChatText.requestFocus();
        }

      public void focusLost(FocusEvent e)
        {
        }
      });

    addFocusListener(new FocusListener()
      {
      public void focusGained(FocusEvent e)
        {
        playerChatText.requestFocus();
        }

      public void focusLost(FocusEvent e)
        {
        }
      });

    // request the focus so key events come to us
    playerChatText.requestFocus();
    requestFocus();

    // create the buffering strategy which will allow AWT
    // to manage our accelerated graphics
    BufferStrategy strategy;
    canvas.createBufferStrategy(2);
    strategy = canvas.getBufferStrategy();

    GameScreen.createScreen(strategy,640,480);
    screen=GameScreen.get();

    inGameGUI=new InGameGUI(client);

    canvas.addMouseListener(inGameGUI);
    canvas.addMouseMotionListener(inGameGUI);

    // add a key input system (defined below) to our canvas so we can respond to key pressed
    playerChatText.addKeyListener(inGameGUI);

    client.setGameLogDialog(new GameLogDialog(this, playerChatText));

    addComponentListener(new ComponentAdapter()
      {
      public void componentHidden(ComponentEvent e)
        {
        }
      public void componentMoved(ComponentEvent e)
        {
        Dimension size=getSize();
        Point location=getLocation();

        client.getGameLogDialog().setLocation(new Point((int)location.getX(), (int)(location.getY()+size.getHeight())));
        }

      public void componentResized(ComponentEvent e)
        {
        }

      public void componentShown(ComponentEvent e)
        {
        }
      });


    // Start the main game loop, note: this method will not
    // return until the game has finished running. Hence we are
    // using the actual main thread to run the game.
    gameLoop();
    }

  public void gameLoop()
    {
    long lastLoopTime = System.currentTimeMillis();
    int fps=0;

    StaticGameLayers staticLayers=client.getStaticGameLayers();
    GameObjects gameObjects=client.getGameObjects();

    long oldTime=System.nanoTime();

    screen.place(-100,-100);
    RenderingPipeline pipeline=RenderingPipeline.get();
    pipeline.addGameLayer(staticLayers);
    pipeline.addGameObjects(gameObjects);

    // keep looping round til the game ends
    while (gameRunning)
      {
      fps++;
      // work out how long its been since the last update, this
      // will be used to calculate how far the entities should
      // move this loop
      long delta = System.currentTimeMillis() - lastLoopTime;
      lastLoopTime = System.currentTimeMillis();

      Logger.trace("j2DClient::gameLoop","D","Move objects");
      gameObjects.move(delta);

      Logger.trace("j2DClient::gameLoop","D","Draw screen");
      pipeline.draw(screen);
      inGameGUI.draw(screen);

      screen.nextFrame();

      Logger.trace("j2DClient::gameLoop","D","Query network");
      client.loop(0);

      Logger.trace("j2DClient::gameLoop","D","Move screen");
      moveScreen(client.getPlayer(),staticLayers,delta);

      if(System.nanoTime()-oldTime>1000000000)
        {
        oldTime=System.nanoTime();
        System.out.println("FPS: "+Integer.toString(fps));
        Logger.trace("j2DCLient::gameLoop()","D","FPS: "+Integer.toString(fps));
        fps=0;
        }

      gameRunning&=client.shouldContinueGame();
      
      Logger.trace("j2DClient::gameLoop","D","Start sleeping");
      long wait=20-delta;
      if(wait>0)
        {
        if(wait>50) 
          {
          Logger.trace("j2DClient::gameLoop","X","Waiting "+wait+" ms");
          wait=50;
          }
          
        try{Thread.sleep(wait);}catch(Exception e){};
        }
        
      Logger.trace("j2DClient::gameLoop","D","End sleeping");
      }

    Logger.trace("j2DClient::gameLoop","!","Request logout");
    client.logout();

    Logger.trace("j2DClient::gameLoop","!","Exit");
    System.exit(0);
    }

  private void moveScreen(RPObject object, StaticGameLayers gameLayers, long delta)
    {
    try
      {
      if(object==null)
        {
        return;
        }

      double x=object.getDouble("x");
      double y=object.getDouble("y");
      double dx=0;
      double dy=0;

      double speed=object.getDouble("speed");

      Direction dir=Direction.build(object.getInt("dir"));
      dx=dir.getdx();
      dy=dir.getdy();

      GameScreen screen=GameScreen.get();
      double screenx=screen.getX();
      double screeny=screen.getY();
      double screenw=screen.getWidth();
      double screenh=screen.getHeight();
      double sdx=screen.getdx();
      double sdy=screen.getdy();

      double dsx=screenx+screenw/2;
      double dsy=screeny+screenh/2;

      double layerw=gameLayers.getWidth();
      double layerh=gameLayers.getHeight();

      if(dsx-x<-2)
        {
        sdx+=0.2;
        }
      else if(dsx-x>-0.5 && dsx-x<0.5)
        {
        sdx/=1.1;
        }
      else if(dsx-x>2)
        {
        sdx-=0.2;
        }


      if(dsy-y<-2)
        {
        sdy+=0.2;
        }
      else if(dsy-y>-0.5 && dsy-y<0.5)
        {
        sdy/=1.1;
        }
      else if(dsy-y>2)
        {
        sdy-=0.2;
        }

      screen.move(sdx,sdy);
      }
    catch(AttributeNotFoundException e)
      {
      //Logger.thrown("j2DClient::moveScreen","X",e);
      }
    }

  public static void main(String args[])
    {
    if(args.length>0)
      {
      int i=0;
      String username=null;
      String password=null;
      String host=null;

      while(i!=args.length)
        {
        if(args[i].equals("-u"))
          {
          username=args[i+1];
          }
        else if(args[i].equals("-p"))
          {
          password=args[i+1];
          }
        else if(args[i].equals("-h"))
          {
          host=args[i+1];
          }
        i++;
        }

      if(username!=null && password!=null && host!=null)
        {
        String[] allowed={"*"/*"j2DClient","StendhalClient"*/};
        Logger.setAllowed(allowed);

        String[] rejected={};
        Logger.setRejected(rejected);

        StendhalClient client=StendhalClient.get();
        try
          {
          client.connect(host,32160);
          client.login(username,password);

          new j2DClient(client).setVisible(true);
          }
        catch(Exception ex)
          {
          ex.printStackTrace();
          }

        return;
        }
      }

    Logger.println("Stendhal j2DClient");
    Logger.println();
    Logger.println("  games.stendhal.j2DClient -u username -p pass -h host -c character");
    Logger.println();
    Logger.println("Required parameters");
    Logger.println("* -h\tHost that is running Marauroa server");
    Logger.println("* -u\tUsername to log into Marauroa server");
    Logger.println("* -p\tPassword to log into Marauroa server");
    }
  }
