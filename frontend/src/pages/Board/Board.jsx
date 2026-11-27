import ProfileLink from '../../components/ProfileLink.jsx'
import {useState,useEffect,useRef} from 'react'
import {useAuth} from '../../context/AuthContext.jsx'
import {useData} from '../../hooks/useData.js'
import {useOptimisticReaction} from '../../hooks/useOptimisticReaction.js'
import {query} from '../../services/apiClient.js'
import * as api from '../../services/boardApi.js'
import {searchWebtoons} from '../../services/webtoonApi.js'
import {go} from '../../router.js'
import {categories} from '../../constants/categories.js'
import {Comments,PostList,Status} from '../shared.jsx'
export function Board({params}){
 const {currentUser}=useAuth();const category=params.get('category')||'',popular=params.get('popular')==='true';const list=useData(()=> (popular?api.fetchPopularPosts:api.fetchPosts)({category}),JSON.stringify([category,popular]),[])
 return <div className="page board-page"><h1>게시판</h1><div className="board-toolbar"><div className="tabs">{[['','전체'],...Object.entries(categories)].map(([k,v])=><a className={k===category?'active':''} key={k} href={'#/board'+query({category:k,popular:popular?'true':''})}>{v}</a>)}</div><div className="board-controls"><label className="switch-label">인기글<input className="switch" type="checkbox" checked={popular} onChange={e=>go('/board'+query({category,popular:e.target.checked?'true':''}))}/></label><a className="button" href={currentUser?'#/board/write':'#/login'}>글쓰기</a></div></div><p className="muted board-caption">{categories[category]||'전체 게시판'}</p><Status state={list}><PostList posts={list.data||[]}/></Status></div>
}
export function PostDetail({ id }) {
  const { currentUser } = useAuth()
  const [version, refresh] = useState(0)
  const viewedPost = useRef(null)
  const state = useData(() => api.fetchPost(id), JSON.stringify([id, currentUser?.id]), null, version)
  const post = state.data
  const likes = useOptimisticReaction(id, currentUser?.id)
  const reaction = likes.reactionFor(id, post?.isLiked, post?.likeCount)

  useEffect(() => {
    if (viewedPost.current?.id !== id) {
      viewedPost.current = { id, promise: api.recordPostView(id) }
    }
    let active = true
    viewedPost.current.promise
      .then(() => { if (active) refresh(value => value + 1) })
      .catch(error => { if (active) console.error(error.message) })
    return () => { active = false }
  }, [id])

  function toggleLike() {
    if (!currentUser) {
      go('/login')
      return
    }
    return likes.toggle(id, reaction, active => api.setPostLike(id, active))
  }

  async function deletePost() {
    if (!confirm('게시글을 삭제할까요?')) return
    try {
      await api.deletePost(id)
      go('/board')
    } catch (error) {
      alert(error.message)
    }
  }

  return (
    <div className="page board-page">
      <Status state={state}>
        {post && (
          <>
            <article className="post-detail">
              <div className="post-title-row">
                <h1>{post.title}</h1>
                <div className="text-actions">
                  {currentUser?.id === post.userId && <a href={'#/board/' + id + '/edit'}>수정</a>}
                  {currentUser && (currentUser.id === post.userId || currentUser.role === 'ADMIN') && (
                    <button onClick={deletePost}>삭제</button>
                  )}
                </div>
              </div>
              <div className="post-meta">
                <span>{categories[post.category]} · <ProfileLink userId={post.userId}>{post.username}</ProfileLink> · {post.createdAt?.slice(0, 10).replaceAll('-', '.')}</span>
                <small>조회 {post.viewCount || 0} · 좋아요 {reaction.count} · 댓글 {post.commentCount || 0}</small>
              </div>
              {post.webtoonId && (
                <a className="review-link" href={'#/webtoon/' + encodeURIComponent(post.webtoonId)}>리뷰 대상 웹툰 보기 →</a>
              )}
              <p className="body-text">{post.content}</p>
              <div className="like-center">
                <button className="secondary" onClick={toggleLike} disabled={reaction.pending} aria-pressed={reaction.active}>
                  {reaction.active ? '♥' : '♡'} 좋아요 {reaction.count}
                </button>
              </div>
            </article>
            <section>
              <h2>게시글 댓글</h2>
              <Comments postId={id} onChange={() => refresh(value => value + 1)} />
            </section>
            <a className="muted" href="#/board">← 게시판 목록</a>
          </>
        )}
      </Status>
    </div>
  )
}
function Editor({post,id,params}){
 const {currentUser,isLoading}=useAuth();const [category,setCategory]=useState(post?.category||params.get('category')||'GENERAL'),[title,setTitle]=useState(post?.title||''),[content,setContent]=useState(post?.content||''),[webtoonId,setWebtoonId]=useState(post?.webtoonId||params.get('webtoonId')||''),[error,setError]=useState('')
 const [keyword,setKeyword]=useState(''),[search,setSearch]=useState(''),[busy,setBusy]=useState(false);const webtoons=useData(()=>searchWebtoons({keyword:search,size:50}),category==='REVIEW'?'review-search:'+search:null,[])
 if(isLoading)return <p>불러오는 중…</p>;if(!currentUser)return <p><a href="#/login">로그인</a>이 필요합니다.</p>;if(post&&post.userId!==currentUser.id)return <p>수정 권한이 없습니다.</p>
 async function submit(e){e.preventDefault();setBusy(true);try{const body={category,title,content,webtoonId:category==='REVIEW'?webtoonId:null};await (id?api.updatePost(id,body):api.createPost(body));go(id?'/board/'+id:'/board')}catch(e){setError(e.message)}finally{setBusy(false)}}
 return <div className="page board-page"><h1>{id?'글 수정':'글쓰기'}</h1><form className="editor" onSubmit={submit}><strong>게시판 유형</strong><div className="tabs category-tabs">{Object.entries(categories).filter(([k])=>k!=='NOTICE'||currentUser.role==='ADMIN').map(([k,v])=><button type="button" className={category===k?'active':'secondary'} aria-pressed={category===k} key={k} onClick={()=>setCategory(k)}>{v}</button>)}</div>
 {category==='REVIEW'&&<div className="review-picker"><label>리뷰 대상 웹툰</label><div className="search-box"><input aria-label="작품 검색" placeholder="작품명 또는 작가 (최대 50개 결과)" value={keyword} onChange={e=>setKeyword(e.target.value)}/><button type="button" onClick={()=>setSearch(keyword)}>작품 검색</button></div><select aria-label="리뷰 대상" required value={webtoonId} onChange={e=>setWebtoonId(e.target.value)}><option value="">작품 선택</option>{webtoonId&&!(webtoons.data||[]).some(w=>w.id===webtoonId)&&<option value={webtoonId}>{webtoonId}</option>}{(webtoons.data||[]).map(w=><option key={w.id} value={w.id}>{w.title}</option>)}</select></div>}
 <input aria-label="제목" placeholder="제목을 입력하세요" required value={title} onChange={e=>setTitle(e.target.value)}/><textarea aria-label="본문" placeholder="본문을 입력하세요" required rows={9} value={content} onChange={e=>setContent(e.target.value)}/>{error&&<p className="error">{error}</p>}<div className="editor-actions"><a className="button secondary" href={id?'#/board/'+id:'#/board'}>취소</a><button disabled={busy}>{id?'저장':'등록'}</button></div></form></div>
}
export function PostEditor({id,params}){const post=useData(()=>api.fetchPost(id),id?'edit:'+id:null);return id?<Status state={post}>{post.data&&<Editor key={id} post={post.data} id={id} params={params}/>}</Status>:<Editor params={params}/>}
