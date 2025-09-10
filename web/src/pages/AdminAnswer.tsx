import React, { useEffect, useState } from 'react'
import { api } from '../lib/api'

export default function AdminAnswer() {
  const [questions, setQuestions] = useState<any[]>([])
  const [selectedQuestion, setSelectedQuestion] = useState<any>(null)
  const [answer, setAnswer] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [aiExplanation, setAiExplanation] = useState('')
  const [explanationInstruction, setExplanationInstruction] = useState('')

  useEffect(() => {
    loadQuestions()
  }, [])

  const loadQuestions = async () => {
    try {
      const response = await api.get('/answers/questions')
      setQuestions(response.data)
    } catch (error) {
      console.error('문제 목록 로드 실패:', error)
    }
  }

  const selectQuestion = async (questionId: number) => {
    try {
      setLoading(true)
      const response = await api.get(`/answers/questions/${questionId}`)
      setSelectedQuestion(response.data.question)
      setAnswer(response.data.resolution)
    } catch (error) {
      console.error('문제 상세 로드 실패:', error)
    } finally {
      setLoading(false)
    }
  }

  const createAnswer = async (outcome: string) => {
    if (!selectedQuestion) return
    
    try {
      await api.post(`/answers/questions/${selectedQuestion.id}`, {
        outcome: outcome,
        proofUrl: '',
        explanation: ''
      })
      alert('정답이 생성되었습니다')
      loadQuestions()
      selectQuestion(selectedQuestion.id)
    } catch (error) {
      console.error('정답 생성 실패:', error)
      alert('정답 생성에 실패했습니다')
    }
  }

  const updateAnswer = async (outcome: string, proofUrl: string) => {
    if (!selectedQuestion) return
    
    try {
      await api.patch(`/answers/questions/${selectedQuestion.id}`, {
        outcome: outcome,
        proofUrl: proofUrl
      })
      alert('정답이 수정되었습니다')
      selectQuestion(selectedQuestion.id)
    } catch (error) {
      console.error('정답 수정 실패:', error)
      alert('정답 수정에 실패했습니다')
    }
  }

  const generateAIExplanation = async () => {
    if (!selectedQuestion || !explanationInstruction) return
    
    try {
      const response = await api.post(`/answers/questions/${selectedQuestion.id}/ai-explanation`, {
        instruction: explanationInstruction
      })
      setAiExplanation(response.data.explanation)
    } catch (error) {
      console.error('AI 해설 생성 실패:', error)
      alert('AI 해설 생성에 실패했습니다')
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">정답 관리</h1>
      
      {/* 문제 목록 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div>
          <h2 className="text-lg font-semibold mb-4">정답이 필요한 문제 목록</h2>
          <div className="space-y-2 max-h-96 overflow-y-auto">
            {questions.map(question => (
              <div 
                key={question.id} 
                className={`p-3 rounded-lg cursor-pointer transition-colors ${
                  selectedQuestion?.id === question.id 
                    ? 'bg-blue-600 text-white' 
                    : 'bg-neutral-800 hover:bg-neutral-700'
                }`}
                onClick={() => selectQuestion(question.id)}
              >
                <div className="font-medium">{question.prompt}</div>
                <div className="text-sm opacity-80">
                  상태: {question.status} | 마감: {new Date(question.closes_at).toLocaleString()}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 선택된 문제의 정답 관리 */}
        <div>
          {selectedQuestion ? (
            <div className="space-y-4">
              <h2 className="text-lg font-semibold">정답 관리</h2>
              
              <div className="p-4 bg-neutral-800 rounded-lg">
                <h3 className="font-medium mb-2">문제: {selectedQuestion.prompt}</h3>
                <p className="text-sm text-gray-400">마감: {new Date(selectedQuestion.closes_at).toLocaleString()}</p>
              </div>

              {answer ? (
                <div className="space-y-4">
                  <div className="p-4 bg-green-900/20 rounded-lg">
                    <h4 className="font-medium mb-2">현재 정답</h4>
                    <p>결과: {answer.outcome}</p>
                    <p>증명 URL: {answer.proof_url || '없음'}</p>
                    <p>생성일: {new Date(answer.resolved_at).toLocaleString()}</p>
                  </div>

                  <div className="space-y-2">
                    <label className="block text-sm font-medium">정답 수정</label>
                    <select 
                      className="w-full p-2 bg-neutral-700 rounded"
                      defaultValue={answer.outcome}
                      onChange={(e) => updateAnswer(e.target.value, answer.proof_url || '')}
                    >
                      <option value="O">O (상승/긍정)</option>
                      <option value="X">X (하락/부정)</option>
                      <option value="VOID">VOID (무효)</option>
                    </select>
                  </div>

                  <div className="space-y-2">
                    <label className="block text-sm font-medium">증명 URL</label>
                    <input 
                      type="url"
                      className="w-full p-2 bg-neutral-700 rounded"
                      defaultValue={answer.proof_url || ''}
                      onChange={(e) => updateAnswer(answer.outcome, e.target.value)}
                      placeholder="https://example.com"
                    />
                  </div>
                </div>
              ) : (
                <div className="space-y-4">
                  <p className="text-gray-400">아직 정답이 생성되지 않았습니다.</p>
                  
                  <div className="space-y-2">
                    <label className="block text-sm font-medium">정답 생성</label>
                    <div className="flex gap-2">
                      <button 
                        onClick={() => createAnswer('O')}
                        className="px-4 py-2 bg-green-600 hover:bg-green-700 rounded"
                      >
                        O (상승/긍정)
                      </button>
                      <button 
                        onClick={() => createAnswer('X')}
                        className="px-4 py-2 bg-red-600 hover:bg-red-700 rounded"
                      >
                        X (하락/부정)
                      </button>
                      <button 
                        onClick={() => createAnswer('VOID')}
                        className="px-4 py-2 bg-gray-600 hover:bg-gray-700 rounded"
                      >
                        VOID (무효)
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {/* AI 해설 생성 */}
              <div className="space-y-2">
                <label className="block text-sm font-medium">AI 해설 생성</label>
                <textarea 
                  className="w-full p-2 bg-neutral-700 rounded"
                  rows={3}
                  value={explanationInstruction}
                  onChange={(e) => setExplanationInstruction(e.target.value)}
                  placeholder="해설 생성 요청사항을 입력하세요..."
                />
                <button 
                  onClick={generateAIExplanation}
                  className="px-4 py-2 bg-purple-600 hover:bg-purple-700 rounded"
                >
                  AI 해설 생성
                </button>
                
                {aiExplanation && (
                  <div className="p-4 bg-purple-900/20 rounded-lg">
                    <h4 className="font-medium mb-2">AI 생성 해설</h4>
                    <p className="whitespace-pre-wrap">{aiExplanation}</p>
                  </div>
                )}
              </div>
            </div>
          ) : (
            <div className="text-gray-400">문제를 선택하세요</div>
          )}
        </div>
      </div>
    </div>
  )
}
