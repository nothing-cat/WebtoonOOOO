import { useEffect, useState } from 'react'

export function useData(load, key, initial = null, version = 0) {
  const [state, setState] = useState({
    key: null,
    version: null,
    data: initial,
    error: '',
    loading: true,
  })

  useEffect(() => {
    if (key === null) return

    let active = true

    Promise.resolve()
      .then(load)
      .then(data => {
        if (active) setState({ key, version, data, error: '', loading: false })
      })
      .catch(error => {
        if (active) setState({ key, version, data: null, error: error.message, loading: false })
      })

    return () => {
      active = false
    }
    // key identifies the request arguments, and version triggers a refresh.
  }, [key, version])

  if (key === null) return { data: initial, error: '', loading: false }
  if (state.key === key && state.version === version) return state

  return { data: initial, error: '', loading: true }
}
