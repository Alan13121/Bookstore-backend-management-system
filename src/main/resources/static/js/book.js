// 書籍編輯頁：從網址取得 id，載入資料填入表單，送出時以 PUT 更新

const BOOK_FORM_FIELDS = {
  title: 'book-title',
  author: 'book-author',
  description: 'book-desc',
  listPrice: 'book-list-price',
  salePrice: 'book-sale-price',
};

function fillBookForm(book) {
  document.getElementById('book-id').textContent = book.id;
  Object.entries(BOOK_FORM_FIELDS).forEach(([key, id]) => {
    document.getElementById(id).value = book[key] ?? '';
  });
}

async function updateBook(id) {
  const fields = readFields(BOOK_FORM_FIELDS);
  const book = {
    ...fields,
    listPrice: toNumberOrNull(fields.listPrice),
    salePrice: toNumberOrNull(fields.salePrice),
  };

  try {
    await apiFetch(`/api/books/${id}`, { method: 'PUT', body: book });
    alert('更新成功');
    location.href = '/book/books.html';
  } catch (err) {
    alert(`更新失敗：${err.message}`);
  }
}

document.addEventListener('DOMContentLoaded', async () => {
  if (!checkAuthOrRedirect()) return;

  const id = getQueryParam('id');
  if (!id) {
    alert('缺少書籍 ID');
    location.href = '/book/books.html';
    return;
  }

  try {
    fillBookForm(await apiFetch(`/api/books/${id}`));
  } catch (err) {
    alert(`載入書籍失敗：${err.message}`);
    location.href = '/book/books.html';
    return;
  }

  document.getElementById('edit-book-form').addEventListener('submit', (e) => {
    e.preventDefault();
    updateBook(id);
  });
});
