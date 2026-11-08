<template>
  <div class="official-page">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-content">
        <div class="avatar">🎮</div>
        <h2>小卢官方 · 摸鱼专区</h2>
        <p>工作累了？来玩两把放松一下吧！</p>
      </div>
    </el-card>

    <div class="games-grid">
      <el-card shadow="hover" class="game-card" @click="goGame('snake')">
        <div class="game-icon">🐍</div>
        <h3>贪吃蛇</h3>
        <p>经典怀旧游戏，方向键控制，看你能吃多长</p>
        <el-button type="primary" round>开始游戏</el-button>
      </el-card>

      <el-card shadow="hover" class="game-card" @click="goGame('tetris')">
        <div class="game-icon">🧱</div>
        <h3>俄罗斯方块</h3>
        <p>经典方块消除，考验你的反应速度</p>
        <el-button type="primary" round>开始游戏</el-button>
      </el-card>

      <el-card shadow="hover" class="game-card" @click="goGame('gomoku')">
        <div class="game-icon">⚫</div>
        <h3>五子棋</h3>
        <p>人机对战，挑战AI看你能赢几局</p>
        <el-button type="primary" round>开始游戏</el-button>
      </el-card>
    </div>

    <el-card shadow="never" class="tips-card">
      <h4>💡 小提示</h4>
      <ul>
        <li>游戏进度和最高分自动保存在本地，刷新不丢失</li>
        <li>上班摸鱼记得随时Alt+Tab切回来哦~</li>
        <li>有什么想玩的其他游戏，跟卢总说！</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goGame(game: string) {
  router.push(`/mini/games/${game}`)
}
</script>

<style scoped>
.official-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.welcome-card {
  margin-bottom: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}
.welcome-content {
  text-align: center;
  color: white;
  padding: 20px 0;
}
.welcome-content .avatar {
  font-size: 64px;
  margin-bottom: 16px;
}
.welcome-content h2 {
  margin: 0 0 8px 0;
  color: white;
}
.welcome-content p {
  margin: 0;
  opacity: 0.9;
}
.games-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}
.game-card {
  text-align: center;
  padding: 20px;
  cursor: pointer;
  transition: transform 0.2s;
}
.game-card:hover {
  transform: translateY(-4px);
}
.game-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
.game-card h3 {
  margin: 0 0 8px 0;
  color: #333;
}
.game-card p {
  color: #666;
  font-size: 14px;
  margin-bottom: 16px;
}
.tips-card {
  background: #f8f9fa;
}
.tips-card h4 {
  margin: 0 0 12px 0;
  color: #333;
}
.tips-card ul {
  margin: 0;
  padding-left: 20px;
  color: #666;
  line-height: 2;
}
</style>
