const base = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/,'')
const readOnlyMethods = new Set(['GET', 'HEAD', 'OPTIONS', 'TRACE'])
let pendingCsrfToken

function fetchCsrfToken() {
  if (!pendingCsrfToken) {
    pendingCsrfToken = fetch(base + '/auth/csrf', { credentials: 'include', cache: 'no-store' })
      .then(async response => {
        if (!response.ok) throw new Error('보안 토큰 요청 실패 (' + response.status + ')')
        return response.json()
      })
      .finally(() => {
        pendingCsrfToken = undefined
      })
  }

  return pendingCsrfToken
}

export async function request(path, method='GET', body) {
  const headers = new Headers()
  if (body) headers.set('Content-Type', 'application/json')
  if (!readOnlyMethods.has(method.toUpperCase())) {
    // Login and logout replace or invalidate the session, so fetch a token for each write.
    const { headerName, token } = await fetchCsrfToken()
    headers.set(headerName, token)
  }

  const response = await fetch(base + path, {
    method,
    credentials: 'include',
    headers,
    body: body ? JSON.stringify(body) : undefined,
  })
  const data=response.status===204?null:await response.json().catch(()=>null)
  if(!response.ok) throw new Error(data?.message || '요청 실패 ('+response.status+')')
  return data
}
export const query=(params)=>{const q=new URLSearchParams(Object.entries(params).filter(([,v])=>v!==''&&v!=null));return q.size?'?'+q:''}
