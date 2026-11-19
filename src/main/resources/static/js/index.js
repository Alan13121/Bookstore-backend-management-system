// 首頁：依登入者的角色動態產生選單。
// 注意：選單只是「顯示」層面的過濾，真正的權限檢查在後端 DynamicAuthorizationFilter，
// 就算使用者自己改網址進入頁面，頁面裡呼叫的 API 仍會被後端擋下。

const MENU_ITEMS = [
  { url: '/book/', label: '📖 書籍管理', page: '/book/books.html' },
  { url: '/user/', label: '👤 用戶管理', page: '/user/users.html' },
  { url: '/api/roles/', label: '🛡️ 角色權限管理', page: '/user/roles.html' },
  { url: '/a_office/', label: 'A部門', page: '/a_office/a_office.html' },
  { url: '/b_office/', label: 'B部門', page: '/b_office/b_office.html' },
  { url: '/c_office/', label: 'C部門', page: '/c_office/c_office.html' },
];

const GUEST_MENU_HTML = `
  <li><a href="/auth/login.html">🔐 登入</a></li>
  <li><a href="/auth/register.html">👥 註冊</a></li>
`;

// 是否有任一條規則同時符合網址與使用者角色
function canAccess(url, userRoles, mappings) {
  return mappings.some((mapping) => {
    try {
      if (!new RegExp(mapping.urlPattern).test(url)) return false;
    } catch (e) {
      console.error('規則不是合法的正規表示式：', mapping.urlPattern);
      return false;
    }
    const allowedRoles = mapping.roles.split(',').map((r) => r.trim());
    return userRoles.some((r) => allowedRoles.includes(r));
  });
}

// 取得公開的權限規則清單（未登入也能呼叫），失敗時回傳空陣列
async function getMappingList() {
  try {
    return await apiFetch('/api/roles/mappings/public');
  } catch (e) {
    console.error('無法取得權限規則', e);
    return [];
  }
}

function logout() {
  clearToken();
  alert('已登出');
  location.reload();
}

async function initMenu() {
  const menu = document.getElementById('menu');

  // 先嘗試延長 token；過期或解不開都視為未登入
  await refreshToken();
  const user = isTokenValid() ? decodeToken(getToken()) : null;
  if (!user || !user.roles) {
    clearToken();
    menu.innerHTML = GUEST_MENU_HTML;
    return;
  }

  // token 裡的角色帶有 ROLE_ 前綴（Spring Security 的慣例），規則裡則沒有
  const userRoles = user.roles.map((r) => r.replace(/^ROLE_/, ''));
  const mappings = await getMappingList();

  menu.innerHTML = '';
  const logoutBtn = document.createElement('button');
  logoutBtn.textContent = '🚪 登出';
  logoutBtn.addEventListener('click', logout);
  menu.appendChild(logoutBtn);

  MENU_ITEMS.filter((item) => canAccess(item.url, userRoles, mappings)).forEach((item) => {
    const li = document.createElement('li');
    const link = document.createElement('a');
    link.textContent = item.label;
    link.href = item.page;
    li.appendChild(link);
    menu.appendChild(li);
  });
}

// 用 pageshow 而不是 DOMContentLoaded：按「上一頁」回到首頁時，瀏覽器可能直接從
// BFCache 還原頁面而不會重新觸發 DOMContentLoaded，pageshow 則每次都會觸發。
window.addEventListener('pageshow', initMenu);
