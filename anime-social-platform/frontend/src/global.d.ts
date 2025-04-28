// 全局声明，以便TypeScript识别全局变量
interface Window {
  global: Window;
}

// 为Node.js模块添加声明
declare module 'sockjs-client'; 