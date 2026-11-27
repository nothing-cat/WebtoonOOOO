import {useEffect, useState} from 'react'
import {Link, Navigate} from 'react-router-dom'
import {useAuth} from '../../context/AuthContext.jsx'
import {useData} from '../../hooks/useData.js'
import {query, request} from '../../services/apiClient.js'
import {fetchGenres, fetchPlatforms} from '../../services/webtoonApi.js'
import {Status} from '../shared.jsx'
import ProfileLink from '../../components/ProfileLink.jsx'
import './Admin.css'

const importPath = '/admin/webtoons/kmas-import'
const importMessages = {
  RUNNING: 'KMAS에서 웹툰을 가져오는 중이에요.',
  COMPLETE: 'KMAS 웹툰 업데이트를 완료했어요.',
  PARTIAL: '일부 플랫폼의 수집이 끝나지 않았어요.',
  FAILED: 'KMAS 웹툰 업데이트를 완료하지 못했어요.',
}
const platformStates = {
  COMPLETE: '완료',
  LIMIT_REACHED: '수집 한도 도달',
  REPEATED_BATCH: '중복 응답으로 중단',
  EMPTY_RESPONSE: '응답 없음',
  INVALID_RESPONSE: '응답 형식 오류',
  API_ERROR: 'API 오류',
  FAILED: '요청 또는 저장 실패',
}

function KmasImport() {
  const [status, setStatus] = useState(null)
  const [starting, setStarting] = useState(false)
  const [error, setError] = useState('')
  const [statusError, setStatusError] = useState('')
  const running = status?.state === 'RUNNING'

  useEffect(() => {
    if (starting) return
    let cancelled = false
    let timer

    async function refresh() {
      try {
        const result = await request(importPath)
        if (cancelled) return
        setStatus(result)
        setStatusError('')
        if (result.state === 'RUNNING') timer = setTimeout(refresh, 3000)
      } catch (error) {
        if (!cancelled) {
          setStatusError(error.message)
          timer = setTimeout(refresh, 5000)
        }
      }
    }

    refresh()
    return () => {
      cancelled = true
      clearTimeout(timer)
    }
  }, [starting])

  async function start() {
    setStarting(true)
    setError('')
    try {
      setStatus(await request(importPath, 'POST'))
    } catch (error) {
      setError(error.message)
    } finally {
      setStarting(false)
    }
  }

  return <section aria-labelledby="kmas-import-title">
    <h2 id="kmas-import-title">KMAS 웹툰 업데이트</h2>
    <p>KMAS에서 작품 정보를 가져와 새 작품을 추가하고 기존 KMAS 작품을 갱신해요.</p>
    <button type="button" disabled={starting || running} onClick={start}>
      {starting || running ? '업데이트 중…' : 'KMAS 데이터 가져오기'}
    </button>
    {(error || statusError) && <p role="alert">{error || statusError}</p>}
    {status && importMessages[status.state] && <p role="status">{importMessages[status.state]}</p>}
    {status?.platforms.length > 0 && <>
      <p>추가 {status.counts.inserted ?? 0}건 · 갱신 {status.counts.updated ?? 0}건</p>
      <ul>
        {status.platforms.map(result => <li key={result.platform}>
          {result.platform} · {platformStates[result.status] ?? result.status} · {result.batches}페이지 저장
        </li>)}
      </ul>
    </>}
  </section>
}

function WebtoonForm() {
  const genres = useData(fetchGenres, 'admin-genres', [])
  const platforms = useData(fetchPlatforms, 'admin-platforms', [])
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [id, setId] = useState(null)

  async function save(event) {
    event.preventDefault()
    const form = event.currentTarget
    const fields = new FormData(form)
    setSaving(true)
    setId(null)
    setError('')
    try {
      const result = await request('/admin/webtoons', 'POST', {
        ...Object.fromEntries(fields),
        platformIds: fields.getAll('platformIds'),
      })
      form.reset()
      setId(result.id)
    } catch (error) {
      setError(error.message)
    } finally {
      setSaving(false)
    }
  }

  return <section>
    <h2>웹툰 수동 등록</h2>
    <form onSubmit={save}>
      <label>제목<input name="title" required /></label>
      <label>그림 작가<input name="pictureWriter" /></label>
      <label>글 작가<input name="storyWriter" /></label>
      <label>줄거리<textarea name="description" /></label>
      <label>썸네일 주소<input name="thumbnailUrl" type="url" /></label>
      <label>연령 등급<input name="ageRating" /></label>
      <Status state={genres}>
        <label>장르<select name="genreId">
          <option value="">선택 안 함</option>
          {(genres.data || []).map(({id, name}) => <option key={id} value={id}>{name}</option>)}
        </select></label>
      </Status>
      <Status state={platforms}>
        <fieldset><legend>연재 플랫폼</legend>
          {(platforms.data || []).map(({id, name}) => <label className="admin-choice" key={id}>
            <input type="checkbox" name="platformIds" value={id} />{name}
          </label>)}
        </fieldset>
      </Status>
      <button disabled={saving || genres.loading || platforms.loading || !!genres.error || !!platforms.error}>등록</button>
      {error && <p role="alert">{error}</p>}
      {id && <p role="status">등록 완료. <Link to={'/webtoon/' + id}>작품 보기</Link></p>}
    </form>
  </section>
}

function Members() {
  const [filter, setFilter] = useState({username: '', page: 0})
  const [version, setVersion] = useState(0)
  const [pending, setPending] = useState(false)
  const [message, setMessage] = useState('')
  const path = '/admin/users' + query(filter)
  const members = useData(() => request(path), path, [], version)

  async function toggle(member) {
    setPending(true)
    setMessage('')
    try {
      await request('/admin/users/' + member.id + '/block', 'PUT', {blocked: !member.blocked})
      setVersion(value => value + 1)
      setMessage(member.username + (member.blocked ? ' 계정 차단을 해제했어요.' : ' 계정을 차단했어요.'))
    } catch (error) {
      setMessage(error.message)
    } finally {
      setPending(false)
    }
  }

  return <section>
    <h2>회원 관리</h2>
    <form onSubmit={event => {
      event.preventDefault()
      setFilter({username: new FormData(event.currentTarget).get('username').trim(), page: 0})
    }}>
      <label>로그인 아이디<input type="search" name="username" /></label>
      <button disabled={pending}>검색</button>
    </form>
    {message && <p role="status">{message}</p>}
    <Status state={members}>
      {members.data?.length === 0 && <p>검색 결과가 없어요.</p>}
      <ul className="admin-members">
        {members.data?.slice(0, 30).map(member => <li key={member.id}>
          <span><ProfileLink userId={member.id}>{member.username}</ProfileLink> · {member.role === 'ADMIN' ? '관리자' : member.blocked ? '차단됨' : '이용 가능'}</span>
          {member.role === 'USER' && <button type="button" disabled={pending} onClick={() => toggle(member)}>
            {member.blocked ? '차단 해제' : '차단'}
          </button>}
        </li>)}
      </ul>
      {(filter.page > 0 || members.data?.length > 30) && <div className="admin-pagination">
        <button disabled={pending || filter.page === 0} onClick={() => setFilter({...filter, page: filter.page - 1})}>이전</button>
        <span>{filter.page + 1}페이지</span>
        <button disabled={pending || !(members.data?.length > 30)} onClick={() => setFilter({...filter, page: filter.page + 1})}>다음</button>
      </div>}
    </Status>
  </section>
}

function BannerForm() {
  const banner = useData(() => request('/banner'), 'admin-banner')
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState('')

  async function save(event) {
    event.preventDefault()
    const fields = Object.fromEntries(new FormData(event.currentTarget))
    setSaving(true)
    setMessage('')
    try {
      await request('/admin/banner', 'PUT', fields)
      setMessage('홈 배너를 저장했어요.')
    } catch (error) {
      setMessage(error.message)
    } finally {
      setSaving(false)
    }
  }

  return <section>
    <h2>홈 배너 편집</h2>
    <Status state={banner}>
      <form onSubmit={save}>
        <label>배너 이미지 주소<input name="imageUrl" type="url" defaultValue={banner.data?.imageUrl} required /></label>
        <label>이미지 대체 텍스트<input name="altText" defaultValue={banner.data?.altText} required /></label>
        <label>클릭 시 이동할 주소<input name="linkUrl" type="url" defaultValue={banner.data?.linkUrl} /></label>
        <button disabled={saving}>저장</button>
        {message && <p role="status">{message}</p>}
      </form>
    </Status>
  </section>
}

function WebtoonManagement() {
  return <div className="page">
    <KmasImport />
    <WebtoonForm />
  </div>
}

const adminTabs = [
  {id: 'webtoons', label: '웹툰 수동 등록', Panel: WebtoonManagement},
  {id: 'members', label: '회원 관리', Panel: Members},
  {id: 'banner', label: '홈 배너 편집', Panel: BannerForm},
]

export default function Admin() {
  const {currentUser, isLoading} = useAuth()
  const [activeTab, setActiveTab] = useState(adminTabs[0].id)
  if (isLoading) return <p role="status">불러오는 중…</p>
  if (!currentUser) return <Navigate to="/login" replace />
  if (currentUser.role !== 'ADMIN') return <Navigate to="/" replace />

  function moveTab(event, index) {
    const last = adminTabs.length - 1
    const destinations = {
      ArrowLeft: index === 0 ? last : index - 1,
      ArrowRight: index === last ? 0 : index + 1,
      Home: 0,
      End: last,
    }
    if (!Object.hasOwn(destinations, event.key)) return
    event.preventDefault()
    const nextTab = adminTabs[destinations[event.key]].id
    setActiveTab(nextTab)
    document.getElementById('admin-tab-' + nextTab)?.focus()
  }

  return <div className="page admin-page">
    <h1>관리자</h1>
    <div className="admin-tabs" role="tablist" aria-label="관리자 기능">
      {adminTabs.map(({id, label}, index) => <button
        key={id}
        type="button"
        role="tab"
        id={'admin-tab-' + id}
        aria-selected={activeTab === id}
        aria-controls={'admin-panel-' + id}
        tabIndex={activeTab === id ? 0 : -1}
        onClick={() => setActiveTab(id)}
        onKeyDown={event => moveTab(event, index)}
      >{label}</button>)}
    </div>
    {adminTabs.map(({id, Panel}) => <div
      key={id}
      role="tabpanel"
      id={'admin-panel-' + id}
      aria-labelledby={'admin-tab-' + id}
      hidden={activeTab !== id}
    ><Panel /></div>)}
  </div>
}
