const base = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/,'')
export async function request(path, method='GET', body) {
  const response=await fetch(base+path,{method,credentials:'include',headers:body?{'Content-Type':'application/json'}:{},body:body?JSON.stringify(body):undefined})
  const data=response.status===204?null:await response.json().catch(()=>null)
  if(!response.ok) throw new Error(data?.message || '요청 실패 ('+response.status+')')
  return data
}
export const query=(params)=>{const q=new URLSearchParams(Object.entries(params).filter(([,v])=>v!==''&&v!=null));return q.size?'?'+q:''}
