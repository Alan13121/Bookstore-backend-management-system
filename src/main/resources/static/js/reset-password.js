// 重設密碼：成功後導回登入頁
const resetForm = document.getElementById('reset-form');

onFormSubmit(resetForm, async () => {
  const output = document.getElementById('output');
  output.textContent = '';
  const body = readFields({ username: 'reset-username', newPassword: 'reset-new-password' });

  try {
    const { message } = await apiFetch('/api/auth/reset-password', { method: 'POST', body });
    flashToast(message);
    location.href = 'login.html';
  } catch (err) {
    output.textContent = err.message;
  }
});
