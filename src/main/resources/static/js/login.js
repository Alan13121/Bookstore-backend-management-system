// 登入：成功後把後端發的 JWT 存進 localStorage，之後每個 API 請求都會帶上它。
// onFormSubmit（common.js）統一處理：e.preventDefault() 阻止整頁提交、欄位驗證、按鈕載入中。
// 用 form 的 submit 事件（而不是按鈕 onclick），按 Enter 也能送出。
const loginForm = document.getElementById('login-form');

onFormSubmit(loginForm, async () => {
  const output = document.getElementById('output');
  output.textContent = '';
  const credentials = readFields({ username: 'login-username', password: 'login-password' });

  try {
    const data = await apiFetch('/api/auth/login', { method: 'POST', body: credentials });
    setToken(data.token);
    // 馬上就要跳頁，所以用 flashToast 讓首頁載入後再顯示
    flashToast('登入成功');
    location.href = '/index.html';
  } catch (err) {
    // 登入失敗的訊息直接顯示在表單下方，比跳出提示更不容易漏看
    output.textContent = err.message;
  }
});
