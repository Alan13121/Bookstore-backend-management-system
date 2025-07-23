function setOutput(content) {
  document.getElementById('output').textContent = JSON.stringify(content, null, 2);
}
function authHeader() {
  const token = localStorage.getItem("token"); 
  return token
    ? { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token }
    : { 'Content-Type': 'application/json' };
}



// 公用的帶 header 載入頁面函數
function loadPageWithToken(urlOrOptions, maybeId) {
  let url;
  let id;

  if (typeof urlOrOptions === 'string') {
    url = urlOrOptions;
    id = maybeId;
  } else if (typeof urlOrOptions === 'object') {
    url = urlOrOptions.url;
    id = urlOrOptions.id;
    if (urlOrOptions.params) {
      const params = new URLSearchParams(urlOrOptions.params);
      url += `?${params.toString()}`;
    }
  } else {
    throw new Error('Invalid arguments');
  }

  const token = localStorage.getItem("token");

  fetch(url.split('?')[0], {
    headers: {
      "Authorization": "Bearer " + token
    }
  })
  .then(res => res.text())
  .then(html => {
    history.pushState(null, '', url);
    if (id) {
      html = html.replace(
        '</body>',
        `<script>window.__USER_ID__=${JSON.stringify(id)}</script></body>`
      );
    }
    document.open();
    document.write(html);
    document.close();
  })
  .catch(err => alert(err.message));
}
