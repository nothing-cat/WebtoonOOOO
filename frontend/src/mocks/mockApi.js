import {genres,platforms,webtoons,initialPosts} from './data.js'
const key='webtoonoooo-demo-v1'
const read=()=>{try{return JSON.parse(localStorage.getItem(key))||null}catch{return null}}
let state=read()||{users:[{id:'demo',username:'demo',role:'USER'},{id:'admin',username:'admin',role:'ADMIN'}],user:null,posts:initialPosts,comments:[],postComments:[],favorites:[],likes:[],next:100}
const save=()=>localStorage.setItem(key,JSON.stringify(state))
const fail=(message)=>{throw new Error(message)}
const auth=()=>state.user||fail('로그인이 필요합니다.')
const canDelete=item=>auth().id===item.userId||state.user.role==='ADMIN'
const now=()=>new Date().toISOString()
const liked=(kind,id)=>state.likes.some(x=>x.kind===kind&&x.id===String(id)&&x.userId===state.user?.id)
const count=(kind,id)=>state.likes.filter(x=>x.kind===kind&&x.id===String(id)).length
const favorite=id=>state.favorites.some(x=>x.webtoonId===id&&x.userId===state.user?.id)
const wview=w=>({...w,likeCount:w.likeCount+state.favorites.filter(x=>x.webtoonId===w.id).length,isFavorite:favorite(w.id)})
const pview=p=>({...p,likeCount:(p.likeCount||0)+count('post',p.id),isLiked:liked('post',p.id),commentCount:state.postComments.filter(c=>c.postId===p.id).length})
const cview=c=>({...c,empathyCount:count('comment',c.id),isLiked:liked('comment',c.id)})
function toggle(kind,id,method){const user=auth();state.likes=state.likes.filter(x=>!(x.kind===kind&&x.id===String(id)&&x.userId===user.id));if(method==='POST')state.likes.push({kind,id:String(id),userId:user.id});save();return null}
export async function mock(path,method='GET',body={}){
 const [pathname,qs]=path.split('?');const q=new URLSearchParams(qs);const parts=pathname.split('/').filter(Boolean).map(decodeURIComponent);const [a,b,c,d]=parts
 if(a==='auth'){
  if(b==='logout'){state.user=null;save();return null}
  if(!/^[a-z0-9_]{4,20}$/.test(body.username||'')||!body.password)fail('아이디 4~20자 및 비밀번호를 입력하세요.')
  if(b==='signup'){if(state.users.some(u=>u.username===body.username))fail('이미 존재하는 아이디입니다.');state.users.push({id:'U'+state.next++,username:body.username,role:'USER'});save();return {message:'가입 완료'}}
  if(b==='login'){const user=state.users.find(u=>u.username===body.username);if(!user)fail('존재하지 않는 데모 아이디입니다.');state.user=user;save();return user}
 }
 if(a==='genres')return genres
 if(a==='platforms')return platforms
 if(a==='users'&&b==='me'){
  const u=auth()
  if(c==='favorites')return state.favorites.filter(x=>x.userId===u.id).sort((a,b)=>b.createdAt.localeCompare(a.createdAt)).map(x=>wview(webtoons.find(w=>w.id===x.webtoonId)))
  if(c==='comments')return state.comments.filter(x=>x.userId===u.id).map(cview)
  if(c==='posts')return state.posts.filter(x=>x.userId===u.id).map(pview)
  if(c==='post-comments')return state.postComments.filter(x=>x.userId===u.id)
  return u
 }
 if(a==='webtoons'){
  if(!b||b==='popular'){let list=webtoons.map(wview).filter(w=>(!q.get('keyword')||[w.title,w.pictureWriter,w.storyWriter].join(' ').includes(q.get('keyword')))&&(!q.get('genre')||w.genreId===q.get('genre'))&&(!q.get('platform')||w.platformIds.includes(q.get('platform'))));if(b==='popular')list.sort((x,y)=>y.likeCount-x.likeCount);return list}
  const w=webtoons.find(w=>w.id===b)||fail('웹툰이 없습니다.')
  if(c==='favorite'){const u=auth();state.favorites=state.favorites.filter(x=>!(x.userId===u.id&&x.webtoonId===b));if(method==='POST')state.favorites.push({userId:u.id,webtoonId:b,createdAt:now()});save();return wview(w)}
  if(c==='reviews')return state.posts.filter(p=>p.category==='REVIEW'&&p.webtoonId===b).map(pview)
  if(c==='comments'){if(method==='GET')return state.comments.filter(x=>x.webtoonId===b).map(cview);const u=auth();if(!body.text?.trim())fail('댓글을 입력하세요.');const item={id:state.next++,webtoonId:b,userId:u.id,username:u.username,text:body.text,createdAt:now()};state.comments.push(item);save();return cview(item)}
  return wview(w)
 }
 if(a==='posts'){
  if((!b||b==='popular')&&method==='GET'){let list=state.posts.filter(p=>!q.get('category')||p.category===q.get('category')).map(pview);return list.sort((x,y)=>b==='popular'?y.likeCount-x.likeCount:y.id-x.id)}
  const validate=()=>{const u=auth();if(!['NOTICE','GENERAL','REVIEW','QUESTION'].includes(body.category)||!body.title?.trim()||!body.content?.trim())fail('필수 항목을 입력하세요.');if(body.category==='NOTICE'&&u.role!=='ADMIN')fail('관리자만 공지를 작성할 수 있습니다.');if(body.category==='REVIEW'&&!webtoons.some(w=>w.id===body.webtoonId))fail('리뷰 웹툰을 선택하세요.');return {category:body.category,title:body.title,content:body.content,webtoonId:body.category==='REVIEW'?body.webtoonId:null}}
  if(!b&&method==='POST'){const v=validate(),u=auth();const p={...v,id:state.next++,userId:u.id,username:u.username,createdAt:now(),viewCount:0,likeCount:0};state.posts.push(p);save();return pview(p)}
  const p=state.posts.find(p=>p.id===Number(b))||fail('게시글이 없습니다.')
  if(c==='like')return toggle('post',p.id,method)
  if(c==='comments'){if(method==='GET')return state.postComments.filter(x=>x.postId===p.id);const u=auth();if(!body.content?.trim())fail('댓글을 입력하세요.');const item={id:state.next++,postId:p.id,userId:u.id,username:u.username,content:body.content,createdAt:now()};state.postComments.push(item);save();return item}
  if(method==='PUT'){if(auth().id!==p.userId)fail('수정 권한이 없습니다.');Object.assign(p,validate(),{updatedAt:now()});save();return pview(p)}
  if(method==='DELETE'){if(!canDelete(p))fail('삭제 권한이 없습니다.');state.posts=state.posts.filter(x=>x.id!==p.id);state.postComments=state.postComments.filter(x=>x.postId!==p.id);state.likes=state.likes.filter(x=>!(x.kind==='post'&&x.id===String(p.id)));save();return null}
  if(method==='GET'){p.viewCount++;save();return pview(p)}
 }
 if(a==='comments'||a==='post-comments'){
  const list=a==='comments'?state.comments:state.postComments;const item=list.find(x=>x.id===Number(b))||fail('댓글이 없습니다.')
  if(c==='like'&&a==='comments')return toggle('comment',item.id,method)
  if(method==='PUT'){if(auth().id!==item.userId)fail('수정 권한이 없습니다.');const field=a==='comments'?'text':'content';if(!body[field]?.trim())fail('댓글을 입력하세요.');item[field]=body[field];item.updatedAt=now();save();return item}
  if(method==='DELETE'){if(!canDelete(item))fail('삭제 권한이 없습니다.');list.splice(list.indexOf(item),1);save();return null}
 }
 fail('지원하지 않는 데모 API: '+path)
}
