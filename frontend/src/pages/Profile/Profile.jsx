import {useState} from 'react'
import {Link,useSearchParams,useNavigate} from 'react-router-dom'
import {useAuth} from '../../context/AuthContext.jsx'
import {useData} from '../../hooks/useData.js'
import {fetchFavorites,fetchMyPosts,fetchMyComments,fetchMyPostComments} from '../../services/userApi.js'
import WebtoonGrid from '../../components/Webtoon/WebtoonGrid.jsx'
import Button from '../../components/ui/Button.jsx'
import SectionTitle from '../../components/ui/SectionTitle.jsx'
import {PostList,Status} from '../shared.jsx'
import {ProfileVisibility} from './PublicProfile.jsx'
import './Profile.css'
function NicknameForm({nickname, saveNickname}) {
  const [value, setValue] = useState(nickname ?? '')
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  const [failed, setFailed] = useState(false)
  const trimmed = value.trim().normalize('NFC')
  const length = [...trimmed].length

  async function submit(event) {
    event.preventDefault()
    if (length < 2 || length > 20 || busy) return
    setBusy(true)
    setMessage('')
    setFailed(false)
    try {
      await saveNickname(trimmed)
      setValue(trimmed)
      setMessage('닉네임을 저장했어요.')
    } catch (error) {
      setMessage(error.message)
      setFailed(true)
    } finally {
      setBusy(false)
    }
  }

  return <form className="profile-nickname" onSubmit={submit}>
    <label htmlFor="profile-nickname">닉네임</label>
    <div>
      <input id="profile-nickname" value={value} onChange={event => {
        setValue(event.target.value)
        setMessage('')
      }} autoComplete="nickname" maxLength={40} aria-describedby="profile-nickname-hint" required />
      <Button type="submit" disabled={busy || length < 2 || length > 20 || trimmed === nickname}>
        {busy ? '저장 중…' : '저장'}
      </Button>
    </div>
    <small id="profile-nickname-hint">2~20자. 게시글과 댓글에 표시돼요.</small>
    {message && <p role={failed ? 'alert' : 'status'}>{message}</p>}
  </form>
}

export default function Profile(){const {currentUser,isLoading,logout,saveNickname}=useAuth();const [params,setParams]=useSearchParams();const navigate=useNavigate();const tab=params.get('tab')||'all';const favorites=useData(fetchFavorites,currentUser?'favorites:'+currentUser.id:null,[]),posts=useData(fetchMyPosts,currentUser?'posts:'+currentUser.id+':'+(currentUser.nickname??''):null,[]),comments=useData(fetchMyComments,currentUser?'comments:'+currentUser.id:null,[]),postComments=useData(fetchMyPostComments,currentUser?'post-comments:'+currentUser.id:null,[]);const visible=k=>tab==='all'||tab===k
if(isLoading)return <div className="page">불러오는 중…</div>;if(!currentUser)return <div className="page"><Link to="/login">로그인</Link>이 필요합니다.</div>
return <div className="page"><section className="card profile-hero"><div><p className="eyebrow">Profile</p><h1>{currentUser.nickname ?? currentUser.username}님의 프로필</h1><p>{currentUser.role==='ADMIN'?'관리자':'일반 회원'}</p></div><Button variant="danger" onClick={()=>logout().then(()=>navigate('/')).catch(e=>alert(e.message))}>로그아웃</Button><NicknameForm key={currentUser.id} nickname={currentUser.nickname} saveNickname={saveNickname}/><ProfileVisibility key={currentUser.id}/></section><div className="card profile-tabs">{[['all','전체 보기'],['favorites','좋아하는 작품'],['comments','웹툰에 남긴 댓글'],['posts','내가 쓴 게시글'],['post-comments','게시글에 남긴 댓글']].map(([k,v])=><button key={k} className={tab===k?'active':''} onClick={()=>setParams({tab:k})}>{v}</button>)}</div>
{visible('favorites')&&<section><SectionTitle title="좋아하는 작품"/><Status state={favorites}><WebtoonGrid webtoons={(favorites.data||[])}/></Status></section>}
{visible('comments')&&<section><SectionTitle title="웹툰에 남긴 댓글"/><Status state={comments}>{!(comments.data||[]).length&&<p>작성한 댓글이 없습니다.</p>}{(comments.data||[]).map(c=><Link className="card activity-row" key={c.id} to={'/webtoon/'+encodeURIComponent(c.webtoonId)}>{c.text}</Link>)}</Status></section>}
{visible('posts')&&<section><SectionTitle title="내가 쓴 게시글"/><Status state={posts}><PostList posts={posts.data||[]}/></Status></section>}
{visible('post-comments')&&<section><SectionTitle title="게시글에 남긴 댓글"/><Status state={postComments}>{!(postComments.data||[]).length&&<p>작성한 댓글이 없습니다.</p>}{(postComments.data||[]).map(c=><Link className="card activity-row" key={c.id} to={'/board/'+c.postId}>{c.content}</Link>)}</Status></section>}</div>}
