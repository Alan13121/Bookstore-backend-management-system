// 首頁：依登入者的角色動態產生選單。
// 注意：選單只是「顯示」層面的過濾，真正的權限檢查在後端 DynamicAuthorizationFilter，
// 就算使用者自己改網址進入頁面，頁面裡呼叫的 API 仍會被後端擋下。

// desc 是首頁卡片上的說明文字
const MENU_ITEMS = [
  { url: '/book/', icon: '📖', label: '書籍管理', desc: '新增、編輯與刪除書籍', page: '/book/books.html' },
  { url: '/user/', icon: '👤', label: '用戶管理', desc: '管理帳號資料與啟用狀態', page: '/user/users.html' },
  { url: '/api/roles/', icon: '🛡️', label: '角色權限管理', desc: '設定角色與 URL 存取規則', page: '/user/roles.html' },
  { url: '/a_office/', icon: '🏢', label: 'A 部門', desc: 'A 部門專區', page: '/a_office/a_office.html' },
  { url: '/b_office/', icon: '🏢', label: 'B 部門', desc: 'B 部門專區', page: '/b_office/b_office.html' },
  { url: '/c_office/', icon: '🏢', label: 'C 部門', desc: 'C 部門專區', page: '/c_office/c_office.html' },
];

const GUEST_MENU_ITEMS = [
  { icon: '🔐', label: '登入', desc: '使用既有帳號登入後台', page: '/auth/login.html' },
  { icon: '👥', label: '註冊', desc: '建立一個新帳號', page: '/auth/register.html' },
];

// 產生一張首頁入口卡片。用 createElement + textContent 而不是組 HTML 字串，
// 文字不會被當成 HTML 解析，就不必擔心跳脫問題。
function createMenuCard({ icon, label, desc, page }) {
  const col = document.createElement('div');
  col.className = 'col-sm-6 col-lg-4';

  const link = document.createElement('a');
  link.className = 'card menu-card';
  link.href = page;

  const body = document.createElement('div');
  body.className = 'card-body d-flex align-items-center gap-3 p-4';

  const iconEl = document.createElement('span');
  iconEl.className = 'menu-icon';
  iconEl.textContent = icon;

  const text = document.createElement('div');
  const title = document.createElement('h2');
  title.className = 'h5 card-title mb-1';
  title.textContent = label;
  const sub = document.createElement('p');
  sub.className = 'small text-secondary mb-0';
  sub.textContent = desc;
  text.append(title, sub);

  body.append(iconEl, text);
  link.append(body);
  col.append(link);
  return col;
}

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
  // reload 後才顯示，所以用 flashToast（存進 sessionStorage，新頁面載入時取出）
  flashToast('已登出', 'info');
  location.reload();
}

async function initMenu() {
  const menu = document.getElementById('menu');
  showLoading(menu);

  // 先嘗試延長 token；過期或解不開都視為未登入
  await refreshToken();
  const user = isTokenValid() ? decodeToken(getToken()) : null;
  if (!user || !user.roles) {
    clearToken();
    menu.replaceChildren(...GUEST_MENU_ITEMS.map(createMenuCard));
    return;
  }

  // token 裡的角色帶有 ROLE_ 前綴（Spring Security 的慣例），規則裡則沒有
  const userRoles = user.roles.map((r) => r.replace(/^ROLE_/, ''));
  const mappings = await getMappingList();

  menu.replaceChildren(
    ...MENU_ITEMS.filter((item) => canAccess(item.url, userRoles, mappings)).map(createMenuCard),
  );

  // 登出按鈕放在導覽列右側；pageshow 可能觸發多次，先移除舊的避免重複
  document.getElementById('logout-btn')?.remove();
  const logoutBtn = document.createElement('button');
  logoutBtn.id = 'logout-btn';
  logoutBtn.type = 'button';
  logoutBtn.className = 'btn btn-link';
  logoutBtn.textContent = user.sub ? `🚪 登出（${user.sub}）` : '🚪 登出';
  logoutBtn.addEventListener('click', logout);
  document.querySelector('.shop-navbar .container').append(logoutBtn);
}

// 用 pageshow 而不是 DOMContentLoaded：按「上一頁」回到首頁時，瀏覽器可能直接從
// BFCache 還原頁面而不會重新觸發 DOMContentLoaded，pageshow 則每次都會觸發。
window.addEventListener('pageshow', initMenu);
