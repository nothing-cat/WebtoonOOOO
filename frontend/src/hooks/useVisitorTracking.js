import {useEffect} from 'react'
import {request} from '../services/apiClient.js'

const storageKey = 'moodtoon.visit'
const seoulDate = new Intl.DateTimeFormat('sv-SE', {timeZone: 'Asia/Seoul'})
let pendingVisit

async function recordVisit() {
  const day = seoulDate.format(new Date())
  const entry = JSON.parse(localStorage.getItem(storageKey) || 'null')
  if (entry?.day === day && entry.recorded) return

  const visitorId = entry?.day === day && entry.visitorId ? entry.visitorId : crypto.randomUUID()
  localStorage.setItem(storageKey, JSON.stringify({day, visitorId, recorded: false}))
  await request('/visits', 'POST', {visitorId})
  localStorage.setItem(storageKey, JSON.stringify({day, visitorId, recorded: true}))
}

export function useVisitorTracking(pathname) {
  useEffect(() => {
    if (!pendingVisit) {
      pendingVisit = recordVisit().catch(() => {}).finally(() => { pendingVisit = undefined })
    }
  }, [pathname])
}
