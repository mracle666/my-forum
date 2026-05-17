import { ref } from 'vue'
import { Client, type IMessage } from '@stomp/stompjs'
import { useUserStore } from '@/stores/user'

const connected = ref(false)
let client: Client | null = null
const subscribers = new Map<string, any>()

export function useWebSocket() {
  const userStore = useUserStore()

  function connect() {
    if (client?.active) return

    try {
      client = new Client({
        brokerURL: 'ws://' + window.location.host + '/ws',
        connectHeaders: userStore.token ? { Authorization: `Bearer ${userStore.token}` } : {},
        debug: () => {},
        reconnectDelay: 3000,
        onConnect: () => {
          connected.value = true
          for (const [dest, cb] of subscribers) {
            try { client?.subscribe(dest, cb) } catch {}
          }
        },
        onDisconnect: () => { connected.value = false },
      })
      client.activate()
    } catch { /* WebSocket not available */ }
  }

  function subscribe(destination: string, callback: (payload: any) => void) {
    subscribers.set(destination, callback)
    if (client?.active) {
      try {
        client.subscribe(destination, (msg: IMessage) => {
          try { callback(JSON.parse(msg.body)) } catch {}
        })
      } catch {}
    }
  }

  function disconnect() {
    subscribers.clear()
    try { client?.deactivate() } catch {}
    client = null
  }

  return { connected, connect, subscribe, disconnect }
}
