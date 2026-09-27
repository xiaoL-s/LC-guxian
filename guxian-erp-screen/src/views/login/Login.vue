<template>
  <!-- 全局渐变背景 -->
  <div class="global-bg">
    <!-- 外层白色大圆角容器（框中框外层容器） -->
    <div class="main-card">
      <!-- 左侧纱窗区域 -->
      <div class="card-left">
        <div class="wireframe"></div>
      </div>
      <!-- 右侧登录表单区域 -->
      <div class="card-right">
        <div class="form-box">
          <h1 class="title">纱窗厂ERP管理系统</h1>
          <p class="desc">生产进度 | 库存管理 | 计件报工 | 订单追踪</p>

          <el-form ref="loginRef" :model="loginForm" label-width="0">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="账号"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                placeholder="密码"
                prefix-icon="Lock"
                show-password
                size="large"
              />
            </el-form-item>
            <el-form-item>
              <el-button class="login-btn" type="primary" @click="handleLogin" size="large">
                登录系统
              </el-button>
            </el-form-item>
          </el-form>

          <div class="version">内部管理系统 v1.0</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginApi } from '@/api/system/login'
import { setToken } from '@/utils/auth'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()
const loginRef = ref()

// 登录表单
const loginForm = ref({
  username: '卢总',
  password: '124300'
})

// 登录逻辑完全沿用你的代码
const handleLogin = async () => {
  try {
    await loginRef.value.validate()
    const res = await loginApi(loginForm.value)
    console.log('【完整返回】', res)
    console.log('【返回data部分】', res.data)
    if (res.code === 200) {
      localStorage.setItem('loginUser', JSON.stringify(res.data))
      console.log('✅成功分支进来了')
      // 先打印token，看能不能拿到
      console.log('token=', res.data.token)
      setToken(res.data.token)
      //userStore.setUserInfo(res.data)
      ElMessage.success('登录成功')
      router.push('/system/').catch(e=>console.log('路由跳转异常',e))
    } else {
      ElMessage.error(res.msg || '账号或密码错误')
    }
  } catch (error) {
    // 重点：打印到底是什么错误！
    console.error('====捕获的异常详情====', error)
    ElMessage.error('登录失败，请检查账号密码或网络')
  }
}
</script>

<style scoped lang="scss">
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.global-bg {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  /* 左右对半分割背景，和原图配色一致 */
  background: linear-gradient(90deg, #eeeeee 50%, #0F3888 50%);

  // 外层白色主卡片，严格维持原图宽高比例 17:8.5
  .main-card {
    width: min(68vw, 1100px);
    aspect-ratio: 17 / 8.5;
    background: #fff;
    border-radius: 22px;
    display: flex;
    overflow: hidden;

    // 左侧区域 50%宽度
    .card-left {
      width: 50%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: url('/favicon.svg') left top / cover no-repeat;

      .wireframe {
        width: 65%;
        height: 70%;
        background: url('/favicon.svg') center / contain no-repeat;
      }
    }

    // 右侧登录区 50%宽度
    .card-right {
      width: 50%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 clamp(40px, 6vw, 130px);

      .form-box {
        width: 100%;

        .title {
          font-size: clamp(24px, 2.4vw, 38px);
          font-weight: 600;
          color: #000;
          margin: 0 0 8px;
        }
        .desc {
          color: #444;
          font-size: clamp(14px, 1vw, 16px);
          margin-bottom: clamp(30px, 4vw, 48px);
        }

        // 修改element样式
        :deep(.el-form-item) {
          margin-bottom: clamp(18px, 2vw, 24px);
        }
        :deep(.el-input__wrapper) {
          height: clamp(46px, 3.2vw, 54px);
          border-radius: 6px;
        }

        .login-btn {
          width: 100%;
          height: clamp(48px, 3.6vw, 56px);
          font-size: clamp(16px, 1.2vw, 18px);
          background-color: #0F3888;
          border: none;
          box-shadow: 0 8px 16px rgba(15, 56, 136, 0.22);
          border-radius: 6px;
          &:hover {
            background: #0b2e70;
          }
        }

        .version {
          text-align: center;
          margin-top: clamp(20px, 2vw, 28px);
          color: #757575;
          font-size: clamp(12px, 0.9vw, 14px);
        }
      }
    }
  }
}

/* 平板/手机：自动变成上下布局 */
@media (max-width: 860px) {
  .global-bg {
    background: #0F3888;
    .main-card {
      flex-direction: column;

      .card-left,
      .card-right {
        width: 100%;
      }

      .card-left {
        height: 45%;
      }
      .card-right {
        height: 55%;
      }
    }
  }
}
</style>