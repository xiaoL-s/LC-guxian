<template>
  <div class="game-page">
    <el-card shadow="never" class="game-card">
      <div class="game-header">
        <h2>🐍 贪吃蛇</h2>
        <div class="score-info">
          <el-tag size="large" type="success">得分：{{ score }}</el-tag>
          <el-tag size="large" type="warning">最高分：{{ highScore }}</el-tag>
        </div>
      </div>
      <div class="game-container">
        <canvas ref="canvasRef" width="400" height="400"></canvas>
        <div v-if="gameOver" class="game-over">
          <h3>游戏结束！</h3>
          <p>得分：{{ score }}</p>
          <el-button type="primary" @click="startGame">再来一局</el-button>
        </div>
        <div v-if="!started && !gameOver" class="game-start">
          <h3>准备开始</h3>
          <p>方向键 / WASD 控制方向</p>
          <el-button type="primary" @click="startGame">开始游戏</el-button>
        </div>
      </div>
      <div class="game-footer">
        <el-button @click="pauseGame" v-if="started && !gameOver">
          {{ paused ? '继续' : '暂停' }}
        </el-button>
        <el-button type="danger" @click="gameOver = true" v-if="started && !gameOver">结束游戏</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const canvasRef = ref<HTMLCanvasElement>()
const score = ref(0)
const highScore = ref(Number(localStorage.getItem('snake_high') || 0))
const gameOver = ref(false)
const started = ref(false)
const paused = ref(false)

let ctx: CanvasRenderingContext2D | null = null
let snake: { x: number; y: number }[] = []
let food = { x: 0, y: 0 }
let direction = 'right'
let nextDirection = 'right'
let gameLoop: number | null = null
const gridSize = 20

onMounted(() => {
  ctx = canvasRef.value?.getContext('2d') || null
  draw()
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  if (gameLoop) clearInterval(gameLoop)
})

function handleKeydown(e: KeyboardEvent) {
  if (!started.value || gameOver.value || paused.value) return
  const key = e.key.toLowerCase()
  if (key === 'arrowup' || key === 'w' && direction !== 'down') nextDirection = 'up'
  else if (key === 'arrowdown' || key === 's' && direction !== 'up') nextDirection = 'down'
  else if (key === 'arrowleft' || key === 'a' && direction !== 'right') nextDirection = 'left'
  else if (key === 'arrowright' || key === 'd' && direction !== 'left') nextDirection = 'right'
}

function startGame() {
  snake = [{ x: 10, y: 10 }, { x: 9, y: 10 }, { x: 8, y: 10 }]
  direction = 'right'
  nextDirection = 'right'
  score.value = 0
  gameOver.value = false
  started.value = true
  paused.value = false
  spawnFood()
  if (gameLoop) clearInterval(gameLoop)
  gameLoop = window.setInterval(tick, 150)
  draw()
}

function pauseGame() {
  paused.value = !paused.value
  if (paused.value) {
    if (gameLoop) clearInterval(gameLoop)
  } else {
    gameLoop = window.setInterval(tick, 150)
  }
}

function spawnFood() {
  let valid = false
  while (!valid) {
    food.x = Math.floor(Math.random() * (400 / gridSize))
    food.y = Math.floor(Math.random() * (400 / gridSize))
    valid = !snake.some(s => s.x === food.x && s.y === food.y)
  }
}

function tick() {
  direction = nextDirection
  const head = { ...snake[0] }
  if (direction === 'up') head.y--
  else if (direction === 'down') head.y++
  else if (direction === 'left') head.x--
  else if (direction === 'right') head.x++

  // 撞墙
  if (head.x < 0 || head.x >= 400 / gridSize || head.y < 0 || head.y >= 400 / gridSize) {
    endGame()
    return
  }
  // 撞自己
  if (snake.some(s => s.x === head.x && s.y === head.y)) {
    endGame()
    return
  }

  snake.unshift(head)

  // 吃食物
  if (head.x === food.x && head.y === food.y) {
    score.value += 10
    if (score.value > highScore.value) {
      highScore.value = score.value
      localStorage.setItem('snake_high', String(highScore.value))
    }
    spawnFood()
  } else {
    snake.pop()
  }

  draw()
}

function endGame() {
  gameOver.value = true
  started.value = false
  if (gameLoop) clearInterval(gameLoop)
}

function draw() {
  if (!ctx) return
  // 背景
  ctx.fillStyle = '#1a1a2e'
  ctx.fillRect(0, 0, 400, 400)

  // 网格线
  ctx.strokeStyle = '#16213e'
  for (let i = 0; i < 400 / gridSize; i++) {
    ctx.beginPath()
    ctx.moveTo(i * gridSize, 0)
    ctx.lineTo(i * gridSize, 400)
    ctx.stroke()
    ctx.beginPath()
    ctx.moveTo(0, i * gridSize)
    ctx.lineTo(400, i * gridSize)
    ctx.stroke()
  }

  // 食物
  ctx.fillStyle = '#e94560'
  ctx.beginPath()
  ctx.arc(food.x * gridSize + gridSize / 2, food.y * gridSize + gridSize / 2, gridSize / 2 - 2, 0, Math.PI * 2)
  ctx.fill()

  // 蛇
  snake.forEach((seg, i) => {
    ctx.fillStyle = i === 0 ? '#0f3460' : '#533483'
    ctx.fillRect(seg.x * gridSize + 1, seg.y * gridSize + 1, gridSize - 2, gridSize - 2)
  })
}
</script>

<style scoped>
.game-page {
  display: flex;
  justify-content: center;
  padding: 20px;
}
.game-card {
  width: 500px;
}
.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.game-header h2 {
  margin: 0;
}
.score-info {
  display: flex;
  gap: 10px;
}
.game-container {
  position: relative;
  display: flex;
  justify-content: center;
}
canvas {
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
}
.game-over, .game-start {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(255,255,255,0.95);
  padding: 30px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0,0,0,0.3);
}
.game-footer {
  margin-top: 20px;
  text-align: center;
  display: flex;
  gap: 10px;
  justify-content: center;
}
</style>
