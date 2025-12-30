# WebSocket 实时通讯前端开发指南

本指南旨在说明如何在 SmartHire 前端项目中利用 WebSocket 实现即时通讯（IM）功能。

## 1. 概述

SmartHire 的即时通讯采用 **WebSocket + REST API** 的混合架构：
- **WebSocket**: 用于实时接收来自服务器的消息推送、系统通知以及心跳检测。
- **REST API**: 用于发送消息、获取历史记录、管理会话等操作。

## 2. 连接与认证

### 2.1 连接地址
WebSocket 服务器地址由基础 URL 和 `/message` 端点组成。
- **开发环境示例**: `ws://localhost:8080/message`
- **生产环境示例**: `wss://api.smarthire.com/message`

### 2.2 身份验证
连接时必须通过查询参数 `token` 传递 JWT Token。
```text
ws://{host}/message?token={your_jwt_token}
```

## 3. 通讯协议

### 3.1 心跳机制 (Heartbeat)
为保持连接活跃，前端需要定期发送心跳包。
- **客户端发送**: 字符串 `"ping"`
- **服务端响应**: 字符串 `"pong"`
- **建议频率**: 每 30 秒发送一次。

### 3.2 接收消息 (Server -> Client)
当有新消息或系统推送时，服务端会通过 WebSocket 发送 JSON 字符串。

**数据格式 (MessageDTO):**
```json
{
  "id": 12345,
  "conversationId": 1,
  "senderId": 101,
  "receiverId": 102,
  "messageType": 1, 
  "content": "你好，请问什么时候有空面试？",
  "fileUrl": null,
  "replyTo": null,
  "isRead": 0,
  "createdAt": "2025-12-24T10:00:00.000+00:00"
}
```
- `messageType` 定义: `1-文本`, `2-图片`, `3-文件`, `4-语音`, `5-视频`

### 3.3 发送消息 (Client -> Server)
发送消息**不通过** WebSocket，而是调用 REST API 接口。

**接口地址**: `POST /message/send-text`
**请求体**:
```json
{
  "receiverId": 102,
  "messageType": 1,
  "content": "明天下午两点可以。"
}
```

## 4. 前端参考实现 (TypeScript)

以下是一个简化的 WebSocket 封装类示例：

```typescript
class ChatSocket {
  private socket: WebSocket | null = null;
  private heartTimer: any = null;
  private url: string;

  constructor(token: string) {
    const baseUrl = process.env.VITE_WS_URL || 'ws://localhost:8080';
    this.url = `${baseUrl}/message?token=${token}`;
  }

  connect() {
    this.socket = new WebSocket(this.url);

    this.socket.onopen = () => {
      console.log('WebSocket 连接成功');
      this.startHeartbeat();
    };

    this.socket.onmessage = (event) => {
      if (event.data === 'pong') return;
      
      try {
        const message = JSON.parse(event.data);
        this.handleNewMessage(message);
      } catch (e) {
        console.error('解析消息失败', e);
      }
    };

    this.socket.onclose = () => {
      console.log('WebSocket 连接关闭');
      this.stopHeartbeat();
      // 可以在此处实现自动重连逻辑
    };

    this.socket.onerror = (error) => {
      console.error('WebSocket 错误', error);
    };
  }

  private startHeartbeat() {
    this.heartTimer = setInterval(() => {
      if (this.socket?.readyState === WebSocket.OPEN) {
        this.socket.send('ping');
      }
    }, 30000);
  }

  private stopHeartbeat() {
    if (this.heartTimer) {
      clearInterval(this.heartTimer);
      this.heartTimer = null;
    }
  }

  private handleNewMessage(message: any) {
    // 这里处理接收到的新消息，例如触发 Vuex/Pinia 的 action 或 EventBus
    console.log('收到新消息:', message);
  }

  disconnect() {
    this.socket?.close();
  }
}
```

## 5. 注意事项

1. **自动重连**: 移动网络环境不稳定，建议实现指数退避重连策略（Exponential Backoff）。
2. **多端登录**: 后端支持同一用户多处登录，所有在线终端都会收到消息推送。
3. **离线消息**: 用户上线时，后端会自动推送未读消息，前端只需监听 `onmessage` 即可。
4. **安全**: 生产环境请务必使用 `wss` 协议。


