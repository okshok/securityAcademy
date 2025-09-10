import React, { useEffect, useState } from 'react'
import { api } from '../lib/api'

export default function AdminQuestion() {
  const [drafts, setDrafts] = useState<any[]>([])
  const [questions, setQuestions] = useState<any[]>([])
  const [selectedItem, setSelectedItem] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [aiInstruction, setAiInstruction] = useState('')
  const [aiResponse, setAiResponse] = useState('')

  useEffect(() => {
    loadDrafts()
    loadQuestions()
  }, [])

  const loadDrafts = async () => {
    try {
      const response = await api.get('/questions/drafts')
      setDrafts(response.data)
    } catch (error) {
      console.error('후보 문제 로드 실패:', error)
    }
  }

  const loadQuestions = async () => {
    try {
      const response = await api.get('/questions')
      setQuestions(response.data)
    } catch (error) {
      console.error('문제 목록 로드 실패:', error)
    }
  }

  const generateCandidates = async () => {
    try {
      setLoading(true)
      await api.post('/batch/generate-candidates')
      alert('문제 후보 생성이 완료되었습니다')
      loadDrafts()
    } catch (error) {
      console.error('후보 생성 실패:', error)
      alert('후보 생성에 실패했습니다')
    } finally {
      setLoading(false)
    }
  }

  const createQuestion = async (candidate: any) => {
    try {
      await api.post('/questions', {
        candidateId: candidate.id,
        prompt: candidate.prompt,
        seasonId: 1,
        ticker: candidate.ticker,
        pros: candidate.pros,
        cons: candidate.cons,
        closesAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString() // 24시간 후
      })
      alert('문제가 생성되었습니다')
      loadQuestions()
    } catch (error) {
      console.error('문제 생성 실패:', error)
      alert('문제 생성에 실패했습니다')
    }
  }

  const confirmQuestion = async (questionId: number) => {
    try {
      await api.patch(`/questions/${questionId}/confirm`)
      alert('문제가 확정되었습니다')
      loadQuestions()
    } catch (error) {
      console.error('문제 확정 실패:', error)
      alert('문제 확정에 실패했습니다')
    }
  }

  const forceCloseQuestion = async (questionId: number) => {
    try {
      await api.patch(`/questions/${questionId}/force-close`)
      alert('문제가 강제 마감되었습니다')
      loadQuestions()
    } catch (error) {
      console.error('문제 마감 실패:', error)
      alert('문제 마감에 실패했습니다')
    }
  }

  const updateQuestion = async (questionId: number, updates: any) => {
    try {
      await api.patch(`/questions/${questionId}`, updates)
      alert('문제가 수정되었습니다')
      loadQuestions()
      if (selectedItem?.id === questionId) {
        setSelectedItem({ ...selectedItem, ...updates })
      }
    } catch (error) {
      console.error('문제 수정 실패:', error)
      alert('문제 수정에 실패했습니다')
    }
  }

  const requestAIUpdate = async (questionId: number) => {
    if (!aiInstruction) return
    
    try {
      const response = await api.post(`/questions/${questionId}/ai-update`, {
        instruction: aiInstruction
      })
      setAiResponse(response.data.aiResponse)
    } catch (error) {
      console.error('AI 수정 요청 실패:', error)
      alert('AI 수정 요청에 실패했습니다')
    }
  }

  const selectItem = async (item: any) => {
    try {
      setLoading(true)
      // 후보 문제인 경우와 생성된 문제인 경우를 구분
      if (item.candidate_date) {
        // 후보 문제인 경우
        setSelectedItem(item)
      } else {
        // 생성된 문제인 경우
        const response = await api.get(`/questions/${item.id}`)
        setSelectedItem(response.data)
      }
    } catch (error) {
      console.error('문제 상세 로드 실패:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">문제 관리</h1>
        <button 
          onClick={generateCandidates}
          disabled={loading}
          className="px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 rounded"
        >
          {loading ? '생성 중...' : '문제 후보 생성'}
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 문제 후보 목록 */}
        <div>
          <h2 className="text-lg font-semibold mb-4">문제 후보 (오늘)</h2>
          <div className="space-y-4 max-h-[600px] overflow-y-auto">
            {drafts.map(d => (
              <div 
                key={d.id} 
                className={`p-4 rounded-lg cursor-pointer transition-all duration-200 border ${
                  selectedItem?.id === d.id 
                    ? 'bg-blue-600 text-white border-blue-500 shadow-lg' 
                    : 'bg-neutral-800 hover:bg-neutral-700 border-neutral-600 hover:border-neutral-500'
                }`}
                onClick={() => selectItem(d)}
              >
                {/* 헤더 정보 */}
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <span className={`px-2 py-1 rounded text-xs font-medium ${
                      d.type === 'EARNINGS' ? 'bg-orange-100 text-orange-800' :
                      d.type === 'MACRO' ? 'bg-purple-100 text-purple-800' :
                      'bg-blue-100 text-blue-800'
                    }`}>
                      {d.type === 'EARNINGS' ? '실적' : d.type === 'MACRO' ? '경제' : '시황'}
                    </span>
                    {d.ticker && (
                      <span className="px-2 py-1 bg-gray-200 text-gray-800 rounded text-xs font-mono">
                        {d.ticker}
                      </span>
                    )}
                    <span className={`px-2 py-1 rounded text-xs ${
                      d.status === 'CANDIDATE' ? 'bg-yellow-100 text-yellow-800' :
                      d.status === 'SELECTED' ? 'bg-green-100 text-green-800' :
                      'bg-gray-100 text-gray-800'
                    }`}>
                      {d.status === 'CANDIDATE' ? '후보' : d.status === 'SELECTED' ? '선택됨' : d.status}
                    </span>
                  </div>
                  <button 
                    onClick={(e) => {
                      e.stopPropagation()
                      createQuestion(d)
                    }}
                    className="px-3 py-1 bg-green-600 hover:bg-green-700 rounded text-sm font-medium transition-colors"
                  >
                    문제 생성
                  </button>
                </div>

                {/* 문제 내용 */}
                <div className="mb-3">
                  <h3 className="font-semibold text-sm mb-1">문제</h3>
                  <p className="text-sm leading-relaxed">{d.prompt}</p>
                </div>

                {/* 찬반 근거 */}
                <div className="grid grid-cols-2 gap-3 text-xs">
                  <div>
                    <h4 className="font-medium mb-1 text-green-400">찬성 근거</h4>
                    <div className="space-y-1">
                      {(() => {
                        try {
                          const pros = JSON.parse(d.pros)
                          return Array.isArray(pros) ? pros.slice(0, 2).map((pro, index) => (
                            <div key={index} className="flex items-start gap-1">
                              <span className="text-green-400 mt-0.5">•</span>
                              <span className="text-xs leading-tight">{pro.text}</span>
                            </div>
                          )) : <span className="text-xs text-gray-400">근거 없음</span>
                        } catch {
                          return <span className="text-xs text-gray-400">근거 없음</span>
                        }
                      })()}
                    </div>
                  </div>
                  <div>
                    <h4 className="font-medium mb-1 text-red-400">반대 근거</h4>
                    <div className="space-y-1">
                      {(() => {
                        try {
                          const cons = JSON.parse(d.cons)
                          return Array.isArray(cons) ? cons.slice(0, 2).map((con, index) => (
                            <div key={index} className="flex items-start gap-1">
                              <span className="text-red-400 mt-0.5">•</span>
                              <span className="text-xs leading-tight">{con.text}</span>
                            </div>
                          )) : <span className="text-xs text-gray-400">근거 없음</span>
                        } catch {
                          return <span className="text-xs text-gray-400">근거 없음</span>
                        }
                      })()}
                    </div>
                  </div>
                </div>

                {/* 생성 시간 */}
                <div className="mt-2 text-xs opacity-60">
                  {new Date(d.created_at).toLocaleString('ko-KR')}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 생성된 문제 목록 */}
        <div>
          <h2 className="text-lg font-semibold mb-4">생성된 문제</h2>
          <div className="space-y-3 max-h-[600px] overflow-y-auto">
            {questions.map(q => (
              <div 
                key={q.id} 
                className={`p-4 rounded-lg cursor-pointer transition-all duration-200 border ${
                  selectedItem?.id === q.id 
                    ? 'bg-blue-600 text-white border-blue-500 shadow-lg' 
                    : 'bg-neutral-800 hover:bg-neutral-700 border-neutral-600 hover:border-neutral-500'
                }`}
                onClick={() => selectItem(q)}
              >
                {/* 헤더 정보 */}
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <span className={`px-2 py-1 rounded text-xs font-medium ${
                      q.status === 'DRAFT' ? 'bg-yellow-100 text-yellow-800' :
                      q.status === 'OPEN' ? 'bg-green-100 text-green-800' :
                      q.status === 'CLOSED' ? 'bg-gray-100 text-gray-800' :
                      'bg-red-100 text-red-800'
                    }`}>
                      {q.status === 'DRAFT' ? '초안' : 
                       q.status === 'OPEN' ? '진행중' : 
                       q.status === 'CLOSED' ? '마감' : q.status}
                    </span>
                    {q.ticker && (
                      <span className="px-2 py-1 bg-gray-200 text-gray-800 rounded text-xs font-mono">
                        {q.ticker}
                      </span>
                    )}
                  </div>
                </div>

                {/* 문제 내용 */}
                <div className="mb-3">
                  <h3 className="font-semibold text-sm mb-1">문제</h3>
                  <p className="text-sm leading-relaxed">{q.prompt}</p>
                </div>

                {/* 상태 정보 */}
                <div className="text-xs opacity-80">
                  <div>마감: {new Date(q.closes_at).toLocaleString('ko-KR')}</div>
                  {q.season_id && <div>시즌: {q.season_id}</div>}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 선택된 문제 상세 관리 */}
      {selectedItem && (
        <div className="mt-6 p-6 bg-neutral-800 rounded-lg">
          <h3 className="text-lg font-semibold mb-4">문제 상세 관리</h3>
          
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1">문제 내용</label>
              <textarea 
                className="w-full p-2 bg-neutral-700 rounded"
                rows={3}
                value={selectedItem.prompt}
                onChange={(e) => setSelectedItem({ ...selectedItem, prompt: e.target.value })}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-1">찬성 근거</label>
                <textarea 
                  className="w-full p-2 bg-neutral-700 rounded"
                  rows={4}
                  value={typeof selectedItem.pros === 'string' ? selectedItem.pros : JSON.stringify(selectedItem.pros, null, 2)}
                  onChange={(e) => setSelectedItem({ ...selectedItem, pros: e.target.value })}
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">반대 근거</label>
                <textarea 
                  className="w-full p-2 bg-neutral-700 rounded"
                  rows={4}
                  value={typeof selectedItem.cons === 'string' ? selectedItem.cons : JSON.stringify(selectedItem.cons, null, 2)}
                  onChange={(e) => setSelectedItem({ ...selectedItem, cons: e.target.value })}
                />
              </div>
            </div>

            {/* 찬반 근거 미리보기 */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-1">찬성 근거 미리보기</label>
                <div className="p-3 bg-green-900/20 rounded border border-green-500/30">
                  {(() => {
                    try {
                      const pros = typeof selectedItem.pros === 'string' ? JSON.parse(selectedItem.pros) : selectedItem.pros
                      return Array.isArray(pros) ? pros.map((pro, index) => (
                        <div key={index} className="text-sm mb-1">• {pro}</div>
                      )) : <div className="text-sm">{selectedItem.pros}</div>
                    } catch {
                      return <div className="text-sm">{selectedItem.pros}</div>
                    }
                  })()}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">반대 근거 미리보기</label>
                <div className="p-3 bg-red-900/20 rounded border border-red-500/30">
                  {(() => {
                    try {
                      const cons = typeof selectedItem.cons === 'string' ? JSON.parse(selectedItem.cons) : selectedItem.cons
                      return Array.isArray(cons) ? cons.map((con, index) => (
                        <div key={index} className="text-sm mb-1">• {con}</div>
                      )) : <div className="text-sm">{selectedItem.cons}</div>
                    } catch {
                      return <div className="text-sm">{selectedItem.cons}</div>
                    }
                  })()}
                </div>
              </div>
            </div>

            <div className="flex gap-2">
              <button 
                onClick={() => updateQuestion(selectedItem.id, {
                  prompt: selectedItem.prompt,
                  pros: selectedItem.pros,
                  cons: selectedItem.cons
                })}
                className="px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded"
              >
                수정 저장
              </button>
              
              {selectedItem.status === 'DRAFT' && (
                <button 
                  onClick={() => confirmQuestion(selectedItem.id)}
                  className="px-4 py-2 bg-green-600 hover:bg-green-700 rounded"
                >
                  문제 확정
                </button>
              )}
              
              <button 
                onClick={() => forceCloseQuestion(selectedItem.id)}
                className="px-4 py-2 bg-red-600 hover:bg-red-700 rounded"
              >
                강제 마감
              </button>
            </div>

            {/* AI 수정 요청 */}
            <div className="border-t pt-4">
              <h4 className="font-medium mb-2">AI 수정 요청</h4>
              <textarea 
                className="w-full p-2 bg-neutral-700 rounded mb-2"
                rows={2}
                value={aiInstruction}
                onChange={(e) => setAiInstruction(e.target.value)}
                placeholder="수정 요청사항을 입력하세요..."
              />
              <button 
                onClick={() => requestAIUpdate(selectedItem.id)}
                className="px-4 py-2 bg-purple-600 hover:bg-purple-700 rounded"
              >
                AI 수정 요청
              </button>
              
              {aiResponse && (
                <div className="mt-2 p-3 bg-purple-900/20 rounded">
                  <h5 className="font-medium mb-1">AI 응답:</h5>
                  <p className="text-sm whitespace-pre-wrap">{aiResponse}</p>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}