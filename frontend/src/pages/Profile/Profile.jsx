import {Link,useSearchParams,useNavigate} from 'react-router-dom'
import {useAuth} from '../../context/AuthContext.jsx'
import {useData} from '../../hooks/useData.js'
import {normalizeWebtoon} from '../../services/webtoonApi.js'
import WebtoonGrid from '../../components/Webtoon/WebtoonGrid.jsx'
import Button from '../../components/ui/Button.jsx'
import SectionTitle from '../../components/ui/SectionTitle.jsx'
import {PostList,Status} from '../shared.jsx'
import './Profile.css'
export default function Profile(){const {currentUser,isLoading,logout}=useAuth();const [params,setParams]=useSearchParams();const navigate=useNavigate();const tab=params.get('tab')||'all';const favorites=useData(currentUser?'/users/me/favorites':null,[]),posts=useData(currentUser?'/users/me/posts':null,[]),comments=useData(currentUser?'/users/me/comments':null,[]),postComments=useData(currentUser?'/users/me/post-comments':null,[]);const visible=k=>tab==='all'||tab===k
if(isLoading)return <div className="page">불러오는 중…</div>;if(!currentUser)return <div className="page"><Link to="/login">로그인</Link>이 필요합니다.</div>
return <div className="page"><section className="card profile-hero"><div><p className="eyebrow">Profile</p><h1>{currentUser.username}님의 프로필</h1><p>{currentUser.role==='ADMIN'?'관리자':'일반 회원'}</p></div><Button variant="danger" onClick={()=>logout().then(()=>navigate('/')).catch(e=>alert(e.message))}>로그아웃</Button></section><div className="card profile-tabs">{[['all','전체 보기'],['favorites','좋아하는 작품'],['comments','웹툰에 남긴 댓글'],['posts','내가 쓴 게시글'],['post-comments','게시글에 남긴 댓글']].map(([k,v])=><button key={k} className={tab===k?'active':''} onClick={()=>setParams({tab:k})}>{v}</button>)}</div>
{visible('favorites')&&<section><SectionTitle title="좋아하는 작품"/><Status state={favorites}><WebtoonGrid webtoons={(favorites.data||[]).map(normalizeWebtoon)}/></Status></section>}
{visible('comments')&&<section><SectionTitle title="웹툰에 남긴 댓글"/><Status state={comments}>{!(comments.data||[]).length&&<p>작성한 댓글이 없습니다.</p>}{(comments.data||[]).map(c=><Link className="card activity-row" key={c.id} to={'/webtoon/'+encodeURIComponent(c.webtoonId)}>{c.text}</Link>)}</Status></section>}
{visible('posts')&&<section><SectionTitle title="내가 쓴 게시글"/><Status state={posts}><PostList posts={posts.data||[]}/></Status></section>}
{visible('post-comments')&&<section><SectionTitle title="게시글에 남긴 댓글"/><Status state={postComments}>{!(postComments.data||[]).length&&<p>작성한 댓글이 없습니다.</p>}{(postComments.data||[]).map(c=><Link className="card activity-row" key={c.id} to={'/board/'+c.postId}>{c.content}</Link>)}</Status></section>}</div>}
