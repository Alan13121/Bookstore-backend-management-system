// 註冊：公開 API，新帳號由後端給予預設角色（AuthService 的 DEFAULT_ROLE）
const registerForm = document.getElementById('register-form');

onFormSubmit(registerForm, async () => {
  const output = document.getElementById('output');
  output.textContent = '';
  const user = readFields({
    username: 'reg-username',
    password: 'reg-password',
    fullName: 'reg-fullname',
    email: 'reg-email',
    phone: 'reg-phone',
  });

  try {
    const { message } = await apiFetch('/api/auth/register', { method: 'POST', body: user });
    flashToast(message);
    location.href = '/auth/login.html';
  } catch (err) {
    // 409 Conflict = 用戶名重複（後端 DuplicateResourceException），
    // 這是某個欄位的問題，所以把訊息標在該欄位下方，而不是只顯示在表單底部
    if (err.status === 409) {
      setFieldError(document.getElementById('reg-username'), err.message);
    } else {
      output.textContent = err.message;
    }
  }
});
