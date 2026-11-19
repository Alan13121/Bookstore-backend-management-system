// 註冊：公開 API，新帳號由後端給予預設角色（AuthService 的 DEFAULT_ROLE）
document.getElementById('register-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const output = document.getElementById('output');
  const user = readFields({
    username: 'reg-username',
    password: 'reg-password',
    fullName: 'reg-fullname',
    email: 'reg-email',
    phone: 'reg-phone',
  });

  try {
    const { message } = await apiFetch('/api/auth/register', { method: 'POST', body: user });
    output.textContent = message;
    alert(message);
    location.href = '/index.html';
  } catch (err) {
    output.textContent = err.message;
    alert(err.message);
  }
});
