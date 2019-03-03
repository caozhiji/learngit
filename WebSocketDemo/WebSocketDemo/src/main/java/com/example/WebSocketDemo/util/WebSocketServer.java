package com.example.WebSocketDemo.util;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

/**
 * @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
 * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 *
 */
@ServerEndpoint("/websocket")
@Component
public class WebSocketServer {
	
	//静态变量，用来记录当前在线连接数。应该把它设计成线程安全的
	private static int onlineCount = 0;
	
	//concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
	//若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
	private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();
	
	//与某个客户端的连接会话，需要通过它来给客户端发送数据
	private Session session;
	
	/**
	 * 连接建立成功调用的方法
	 * @param session 通过它给客户端发送数据
	 */
	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
		webSocketSet.add(this);//加入到set
		addOnlineCount();//在线数加1
		System.out.println("有新连接加入！当前连接数量"+getOnlineCount());
	}
	
	/**
	 * 关闭连接的方法
	 */
	@OnClose
	public void onClose() {
		webSocketSet.remove(this);
		subOnlineCount();//在线数减1
		System.out.println("有一人断开！当前在线人数:"+getOnlineCount());
	}
	
	/**
	 * 收到客户端消息后调用的方法
	 * @param message 客户端发送过来的参数
	 * @param session 可选参数
	 */
	@OnMessage
	public void onMessage(String message,Session session) {
		System.out.println("收到客户端的消息:"+message);
		//群发消息
		for(WebSocketServer item:webSocketSet) {
			try {
				item.sendMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sendMessage(String message) throws IOException {
		this.session.getBasicRemote().sendText(message);
	}

	/**
	 * 发生错误时调用
	 * @param session
	 * @param err
	 */
	@OnError
	public void onError(Session session,Throwable err) {
		System.out.println("发生错误！");
		err.printStackTrace();
	}

	public static synchronized void subOnlineCount() {
		onlineCount--;
	}

	public static synchronized int getOnlineCount() {
		return onlineCount;
	}

	public static synchronized void addOnlineCount() {
		onlineCount++;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
