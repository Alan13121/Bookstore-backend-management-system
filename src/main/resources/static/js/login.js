// 登入：成功後把後端發的 JWT 存進 localStorage，之後每個 API 請求都會帶上它。
// 用 form 的 submit 事件（而不是按鈕 onclick），按 Enter 也能送出，
// 並用 e.preventDefault() 阻止瀏覽器預設的整頁表單提交。
document.getElementById('login-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const output = document.getElementById('output');
  const credentials = readFields({ username: 'login-username', password: 'login-password' });

  try {
    const data = await apiFetch('/api/auth/login', { method: 'POST', body: credentials });
    setToken(data.token);
    alert('登入成功');
    location.href = '/index.html';
  } catch (err) {
    output.textContent = err.message;
    alert('登入失敗');
  }
});
