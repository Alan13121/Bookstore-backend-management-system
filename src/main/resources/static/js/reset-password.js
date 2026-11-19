// 重設密碼：成功後導回登入頁
document.getElementById('reset-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const output = document.getElementById('output');
  const body = readFields({ username: 'reset-username', newPassword: 'reset-new-password' });

  try {
    const { message } = await apiFetch('/api/auth/reset-password', { method: 'POST', body });
    output.textContent = message;
    alert('密碼重設成功');
    location.href = 'login.html';
  } catch (err) {
    output.textContent = err.message;
    alert('密碼重設失敗');
  }
});
