import {useState} from 'react'
import {Link, useParams} from 'react-router-dom'
import {useAuth} from '../../context/AuthContext.jsx'
import {useData} from '../../hooks/useData.js'
import {request} from '../../services/apiClient.js'
import {Status} from '../shared.jsx'
import './Profile.css'

const visibilityPath = '/users/me/profile-visibility'
const options = [
  ['favorites', '좋아하는 작품'],
  ['posts', '작성한 게시글'],
  ['comments', '작성한 댓글'],
]

function VisibilityForm({settings}) {
  const [values, setValues] = useState(settings)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState(null)

  async function save(event) {
    event.preventDefault()
    setBusy(true)
    setMessage(null)
    try {
      setValues(await request(visibilityPath, 'PUT', values))
      setMessage({text: '공개 설정을 저장했어요.'})
    } catch (error) {
      setMessage({text: error.message, error: true})
    } finally {
      setBusy(false)
    }
  }

  return <form className="profile-visibility" onSubmit={save}>
    <fieldset disabled={busy}>
      <legend>프로필 공개 범위</legend>
      <p>선택한 항목을 방문자에게 공개해요. 관리자는 모든 항목을 볼 수 있어요.</p>
      {options.map(([key, label]) => <label key={key}>
        <input type="checkbox" checked={values[key]} onChange={event => {
          setValues({...values, [key]: event.target.checked})
          setMessage(null)
        }} />
        {label}
      </label>)}
      <button className="secondary" type="submit">{busy ? '저장 중…' : '공개 설정 저장'}</button>
    </fieldset>
    {message && <p role={message.error ? 'alert' : 'status'}>{message.text}</p>}
  </form>
}

export function ProfileVisibility() {
  const status = useData(() => request(visibilityPath), visibilityPath)
  return <Status state={status}>
    {status.data && <VisibilityForm settings={status.data} />}
  </Status>
}

function Activity({title, items, path, favorite = false}) {
  if (!items) return null
  return <section>
    <h2>{title}</h2>
    {items.length ? <ul className="profile-public-list">
      {items.map(item => <li key={item.id}>
        <Link to={path + encodeURIComponent(favorite ? item.id : item.targetId)}>
          {favorite && item.thumbnailUrl && <img src={item.thumbnailUrl} alt="" loading="lazy" />}
          <span>{favorite ? item.title : item.text}</span>
        </Link>
      </li>)}
    </ul> : <p className="muted">표시할 활동이 없어요.</p>}
  </section>
}

export default function PublicProfile() {
  const {id} = useParams()
  const {currentUser} = useAuth()
  const path = '/profiles/' + encodeURIComponent(id)
  const state = useData(() => request(path), path + ':' + (currentUser?.id ?? 'guest') + ':' + (currentUser?.role ?? ''))
  const profile = state.data

  return <div className="page">
    <Status state={state}>
      {profile && <>
        <section className="card profile-hero"><h1>{profile.name}님의 프로필</h1></section>
        {!(profile.favorites || profile.posts || profile.webtoonComments) &&
          <p className="muted">이 프로필에 공개된 활동이 없어요.</p>}
        <Activity title="좋아하는 작품" items={profile.favorites} path="/webtoon/" favorite />
        <Activity title="작성한 게시글" items={profile.posts} path="/board/" />
        <Activity title="웹툰에 남긴 댓글" items={profile.webtoonComments} path="/webtoon/" />
        <Activity title="게시글에 남긴 댓글" items={profile.postComments} path="/board/" />
      </>}
    </Status>
  </div>
}
