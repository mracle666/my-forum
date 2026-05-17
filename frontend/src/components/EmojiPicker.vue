<template>
  <el-popover placement="top" :width="320" trigger="click">
    <template #reference>
      <span class="emoji-trigger" title="表情">😊</span>
    </template>
    <div class="emoji-panel">
      <div class="emoji-tabs">
        <span
          v-for="cat in categories"
          :key="cat.key"
          class="emoji-tab"
          :class="{ active: activeCat === cat.key }"
          @click="activeCat = cat.key"
        >{{ cat.icon }}</span>
      </div>
      <div class="emoji-grid">
        <span
          v-for="e in currentEmojis"
          :key="e"
          class="emoji-item"
          @click="$emit('select', e)"
        >{{ e }}</span>
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

defineEmits<{ select: [emoji: string] }>()

const activeCat = ref('face')

const categories = [
  { key: 'face', icon: '😀' },
  { key: 'gesture', icon: '👍' },
  { key: 'heart', icon: '❤️' },
  { key: 'animal', icon: '🐱' },
  { key: 'food', icon: '🍔' },
  { key: 'other', icon: '💡' },
]

const emojiMap: Record<string, string[]> = {
  face: ['😀','😃','😄','😁','😆','😅','🤣','😂','🙂','😊','😇','😍','🤩','😘','😗','😚','😋','😛','😜','🤪','😝','🤑','🤗','🤭','🤫','🤔','🤐','😐','😑','😶','😏','😒','🙄','😬','😮','😯','😲','😳','🥺','😢','😭','😤','😡','🤬','😈','👿','💀','☠️','🤡','👻','👽','🤖','😺','😸','😹','😻','😼','😽','🙀','😿','😾'],
  gesture: ['👍','👎','👌','✌️','🤞','🤟','🤘','🤙','👋','🤚','🖐️','✋','🖖','👏','🙌','🦶','👂','👃','🧠','👀','👁️','👅','👄','💪','🤳','🙏','💅','🤝'],
  heart: ['❤️','🧡','💛','💚','💙','💜','🖤','🤍','🤎','💔','❣️','💕','💞','💓','💗','💖','💘','💝','💟','☮️','✝️','☪️','🕉️','☸️','✡️','🔯','🕎','☯️','☦️','🛐','⚛️','🉑','☢️','☣️','📴','📳','🈶','🈚','🈸','🈺','🈷️','✴️','🆚','💮','🉐'],
  animal: ['🐶','🐱','🐭','🐹','🐰','🦊','🐻','🐼','🐨','🐯','🦁','🐮','🐷','🐸','🐵','🐔','🐧','🐦','🐤','🐣','🐥','🦆','🦅','🦉','🦇','🐺','🐗','🐴','🦄','🐝','🐛','🦋','🐌','🐞','🐜','🦟','🦗','🕷️','🦂','🐢','🐍','🦎','🦖','🦕','🐙','🦑','🦐','🦞','🦀','🐡','🐠','🐟','🐬','🐳','🐋','🦈','🐊','🐅','🐆','🦓','🦍','🐘','🦛','🦏','🐪','🐫','🦒','🦘','🐃','🐂','🐄','🐎','🐖','🐏','🐑','🦙','🐐','🦌','🐕','🐩','🦮','🐈','🐓','🦃','🦚','🦜','🦢','🦩','🕊️','🐇','🦝','🦨','🦡','🦦','🦥','🐁','🐀','🐿️','🦔'],
  food: ['🍏','🍎','🍐','🍊','🍋','🍌','🍉','🍇','🍓','🍈','🍒','🍑','🥭','🍍','🥥','🥝','🍅','🍆','🥑','🥦','🥬','🥒','🌶️','🌽','🥕','🧄','🧅','🥔','🍠','🥐','🍞','🥖','🥨','🧀','🥚','🍳','🧈','🥞','🧇','🥓','🥩','🍗','🍖','🦴','🌭','🍔','🍟','🍕','🥪','🥙','🧆','🌮','🌯','🥗','🥘','🥫','🍝','🍜','🍲','🍛','🍣','🍱','🥟','🦪','🍤','🍙','🍚','🍘','🍥','🥠','🥮','🍢','🍡','🍧','🍨','🍦','🥧','🧁','🍰','🎂','🍮','🍭','🍬','🍫','🍿','🍩','🍪','🌰','🥜','🍯','🥛','🍼','☕','🍵','🧃','🥤','🍶','🍺','🍻','🥂','🍷','🥃','🍸','🍹','🧉','🍾','🧊','🥄','🍴','🍽️','🥣','🥡','🥢','🧂'],
  other: ['⚽','🏀','🏈','⚾','🥎','🎾','🏐','🏉','🥏','🎱','🪀','🏓','🏸','🏒','🏑','🥍','🏏','🪃','🥅','⛳','🪁','🏹','🎣','🤿','🥊','🥋','🎽','🛹','🛷','⛸️','🥌','🎿','⛷️','🏂','🪂','🏋️','🤼','🤸','🤺','⛹️','🤾','🏌️','🏇','🧘','🏄','🏊','🤽','🚣','🧗','🚵','🚴','🏆','🥇','🥈','🥉','🏅','🎖️','🏵️','🎗️','🎫','🎟️','🎪','🤹','🎭','🩰','🎨','🎬','🎤','🎧','🎼','🎹','🥁','🎷','🎺','🎸','🪕','🎻','🎲','♟️','🎯','🎳','🎮','👾','🎰','💡','🔦','📖','🔖','📚','📝','✏️','💼','📁','🗂️','📅','📌','📍','📎','🖇️','🔗','✂️','📏','📐','🔒','🔓','🔑','🗝️','🔨','⚒️','🛠️','⛏️','🔧','🔩','⚙️','🧲','🔫','💣','🧨','🪓','🔪','🗡️','⚔️','🛡️','🚬','⚰️','⚱️','🏺','🔮','📿','🧿','💈','⚖️','🧳'],
}

const currentEmojis = computed(() => emojiMap[activeCat.value] || [])
</script>

<style scoped>
.emoji-trigger {
  cursor: pointer;
  font-size: 18px;
  user-select: none;
  opacity: 0.7;
  transition: opacity 0.2s;
}
.emoji-trigger:hover { opacity: 1; }

.emoji-panel {
  max-height: 280px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.emoji-tabs {
  display: flex;
  gap: 4px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
  margin-bottom: 8px;
}
.emoji-tab {
  cursor: pointer;
  font-size: 16px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}
.emoji-tab:hover { background: #f0f0f0; }
.emoji-tab.active { background: #ecf5ff; }

.emoji-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
  overflow-y: auto;
  max-height: 220px;
}
.emoji-item {
  cursor: pointer;
  font-size: 20px;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.15s;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.emoji-item:hover { background: #ecf5ff; transform: scale(1.2); }
</style>
