import React, { useState, useRef, useContext } from 'react';
import axios from '../axios';
import { useNavigate } from 'react-router-dom';
import AppContext from '../Context/Context';

const AiChatWidget = () => {
  const [isOpen, setIsOpen] = useState(false);
  const { addToCart } = useContext(AppContext);
  const navigate = useNavigate();
  const inputRef = useRef(null);

  const [messages, setMessages] = useState([
    {
      id: 1,
      message: "👋 Hi! I'm your **Autonomous Shopping Agent**.\n\nI can execute store actions on your behalf (like adding products to your cart, applying discount coupons, or tracking orders) using **Hybrid RAG** and real-time tool calling.",
      sender: 'assistant',
      direction: 'incoming',
      toolCalls: [],
      thoughtProcess: null,
    },
  ]);
  const [isTyping, setIsTyping] = useState(false);
  const [suggestedProducts, setSuggestedProducts] = useState([]);
  const [expandedThoughtId, setExpandedThoughtId] = useState(null);
  const [agentNotification, setAgentNotification] = useState(null);

  const handleSend = async (messageText) => {
    if (!messageText || !messageText.trim()) return;

    const userMessage = {
      id: Date.now(),
      message: messageText,
      sender: 'user',
      direction: 'outgoing',
    };

    setMessages((prev) => [...prev, userMessage]);
    setIsTyping(true);
    setSuggestedProducts([]);

    try {
      const response = await axios.post('/api/chat/assistant', { message: messageText });
      const { reply, thoughtProcess, toolCalls, actions, products, citations } = response.data;

      
      if (actions && actions.length > 0) {
        actions.forEach((act) => {
          if (act.type === 'ADD_TO_CART' && act.product) {
            addToCart(act.product);
            showNotification(`🛒 Agent Action: Added "${act.product.name}" to your cart!`);
          } else if (act.type === 'APPLY_COUPON') {
            showNotification(`🏷️ Coupon "${act.couponCode}" applied: ${act.discountAmount}% discount!`);
          }
        });
      }

      const aiMessage = {
        id: Date.now() + 1,
        message: reply,
        sender: 'assistant',
        direction: 'incoming',
        thoughtProcess: thoughtProcess,
        toolCalls: toolCalls || [],
        citations: citations || [],
      };

      setMessages((prev) => [...prev, aiMessage]);
      if (products && products.length > 0) {
        setSuggestedProducts(products);
      }
    } catch (error) {
      console.error('Agent chat failed:', error);
      const errMessage = {
        id: Date.now() + 1,
        message: '⚠️ Sorry, I had trouble connecting with the AI agent. Please try again.',
        sender: 'assistant',
        direction: 'incoming',
        toolCalls: [],
      };
      setMessages((prev) => [...prev, errMessage]);
    } finally {
      setIsTyping(false);
    }
  };

  const showNotification = (msg) => {
    setAgentNotification(msg);
    setTimeout(() => {
      setAgentNotification(null);
    }, 4500);
  };

  const baseUrl = import.meta.env.VITE_BASE_URL;

  const quickPrompts = [
    'Add Sony headphones to cart',
    'Laptops under ₹1.5L',
    'Track order ORD5DB06215',
    'Apply coupon WELCOME10',
    'Compare HP Spectre and Acer Predator',
  ];

  
  const renderMessageContent = (text) => {
    if (!text) return null;

    
    const citationRegex = /\[Source:\s*([^|\]]+?)\s*\|\s*ID:\s*(\d+)\s*\|\s*([^\]]+?)\]/g;
    const parts = [];
    let lastIndex = 0;
    let match;

    while ((match = citationRegex.exec(text)) !== null) {
      if (match.index > lastIndex) {
        parts.push(text.substring(lastIndex, match.index));
      }

      const productName = match[1].trim();
      const productId = match[2].trim();
      const productPrice = match[3].trim();

      parts.push(
        <button
          key={`cite-${match.index}`}
          onClick={() => {
            navigate(`/product/${productId}`);
            setIsOpen(false);
          }}
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '4px',
            background: 'rgba(99, 102, 241, 0.15)',
            border: '1px solid rgba(99, 102, 241, 0.4)',
            color: 'var(--primary-accent, #6366f1)',
            padding: '2px 8px',
            borderRadius: '6px',
            fontSize: '11px',
            fontWeight: 600,
            cursor: 'pointer',
            margin: '2px 4px',
            verticalAlign: 'baseline',
          }}
          title={`View ${productName}`}
        >
          <span>🔗</span>
          <span>{productName}</span>
          <span style={{ opacity: 0.85 }}>({productPrice})</span>
        </button>
      );

      lastIndex = match.index + match[0].length;
    }

    if (lastIndex < text.length) {
      parts.push(text.substring(lastIndex));
    }

    
    return parts.map((part, pIdx) => {
      if (typeof part !== 'string') return part;

      const boldParts = part.split(/(\*\*.*?\*\*)/g);
      return boldParts.map((bPart, bIdx) => {
        if (bPart.startsWith('**') && bPart.endsWith('**')) {
          return <strong key={`${pIdx}-${bIdx}`}>{bPart.slice(2, -2)}</strong>;
        }
        return bPart;
      });
    });
  };

  return (
    <>
      
      {!isOpen && (
        <button
          onClick={() => setIsOpen(true)}
          style={{
            position: 'fixed',
            bottom: '24px',
            right: '24px',
            width: '60px',
            height: '60px',
            borderRadius: '50%',
            background: 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)',
            border: 'none',
            boxShadow: '0 8px 25px rgba(79, 70, 229, 0.45)',
            cursor: 'pointer',
            zIndex: 1000,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            transition: 'all 0.25s ease',
            fontSize: '26px',
          }}
          onMouseEnter={(e) => (e.currentTarget.style.transform = 'scale(1.08) translateY(-2px)')}
          onMouseLeave={(e) => (e.currentTarget.style.transform = 'scale(1) translateY(0)')}
          title="Agentic AI Shopping Concierge"
        >
          ✨
        </button>
      )}

      
      {isOpen && (
        <div
          className="ai-chat-drawer card-theme"
          style={{
            position: 'fixed',
            bottom: '24px',
            right: '24px',
            width: '430px',
            maxWidth: 'calc(100vw - 32px)',
            height: '640px',
            borderRadius: '20px',
            boxShadow: '0 24px 60px rgba(0,0,0,0.35)',
            zIndex: 1001,
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
            background: 'var(--bg-surface, #ffffff)',
            border: '1px solid var(--border-color, #e5e7eb)',
            animation: 'slideUp 0.25s cubic-bezier(0.16, 1, 0.3, 1)',
          }}
        >
          
          <div
            style={{
              background: 'linear-gradient(135deg, #1e1b4b 0%, #312e81 100%)',
              padding: '14px 18px',
              color: '#ffffff',
              flexShrink: 0,
            }}
          >
            <div className="d-flex align-items-center justify-content-between">
              <div className="d-flex align-items-center gap-2">
                <div
                  style={{
                    width: '34px',
                    height: '34px',
                    borderRadius: '10px',
                    background: 'rgba(255,255,255,0.15)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '18px',
                  }}
                >
                  🤖
                </div>
                <div>
                  <div className="fw-bold" style={{ fontSize: '14px', letterSpacing: '-0.2px', color: '#ffffff' }}>
                    Lokendra Agentic Concierge
                  </div>
                  <div className="d-flex align-items-center gap-1" style={{ fontSize: '11px', color: 'rgba(255,255,255,0.85)' }}>
                    <span
                      style={{
                        display: 'inline-block',
                        width: '7px',
                        height: '7px',
                        borderRadius: '50%',
                        backgroundColor: '#10b981',
                        boxShadow: '0 0 6px #10b981',
                      }}
                    />
                    <span>ReAct Loop · Hybrid RAG Active</span>
                  </div>
                </div>
              </div>

              <div className="d-flex align-items-center gap-2">
                <span
                  className="badge"
                  style={{
                    background: 'rgba(99, 102, 241, 0.3)',
                    border: '1px solid rgba(165, 180, 252, 0.4)',
                    fontSize: '10px',
                    fontWeight: 600,
                    padding: '4px 8px',
                    color: '#ffffff',
                  }}
                >
                  AGENT v2
                </span>
                <button
                  onClick={() => setIsOpen(false)}
                  style={{
                    background: 'rgba(255, 255, 255, 0.15)',
                    border: 'none',
                    color: '#ffffff',
                    borderRadius: '50%',
                    width: '28px',
                    height: '28px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    cursor: 'pointer',
                    fontSize: '13px',
                    transition: 'background 0.15s ease',
                  }}
                  onMouseEnter={(e) => (e.currentTarget.style.background = 'rgba(255, 255, 255, 0.3)')}
                  onMouseLeave={(e) => (e.currentTarget.style.background = 'rgba(255, 255, 255, 0.15)')}
                  title="Close Assistant"
                >
                  ✕
                </button>
              </div>
            </div>
          </div>

          
          {agentNotification && (
            <div
              style={{
                background: '#064e3b',
                color: '#a7f3d0',
                borderBottom: '1px solid #059669',
                padding: '9px 14px',
                fontSize: '12px',
                fontWeight: 600,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                animation: 'fadeIn 0.2s ease',
              }}
            >
              <span>{agentNotification}</span>
              <button
                onClick={() => {
                  navigate('/cart');
                  setIsOpen(false);
                }}
                className="btn btn-sm btn-link text-decoration-none p-0 fw-bold"
                style={{ fontSize: '11px', color: '#6ee7b7' }}
              >
                View Cart →
              </button>
            </div>
          )}

          
          {messages.length <= 1 && (
            <div
              style={{
                padding: '10px 14px',
                background: 'var(--bg-subtle, #f9fafb)',
                borderBottom: '1px solid var(--border-color, #e5e7eb)',
                flexShrink: 0,
              }}
            >
              <div
                style={{ fontSize: '10px', color: 'var(--text-muted, #94a3b8)', marginBottom: '6px', fontWeight: 700, letterSpacing: '0.5px' }}
              >
                ⚡ QUICK AGENT COMMANDS:
              </div>
              <div className="d-flex flex-wrap gap-1">
                {quickPrompts.map((p) => (
                  <button
                    key={p}
                    onClick={() => handleSend(p)}
                    style={{
                      fontSize: '11px',
                      padding: '4px 10px',
                      borderRadius: '16px',
                      border: '1px solid var(--border-color, #334155)',
                      background: 'var(--bg-surface, #1e293b)',
                      color: 'var(--primary-accent, #818cf8)',
                      cursor: 'pointer',
                      fontWeight: 500,
                      transition: 'all 0.15s ease',
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.background = '#4f46e5';
                      e.currentTarget.style.color = '#ffffff';
                      e.currentTarget.style.borderColor = '#4f46e5';
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.background = 'var(--bg-surface, #1e293b)';
                      e.currentTarget.style.color = 'var(--primary-accent, #818cf8)';
                      e.currentTarget.style.borderColor = 'var(--border-color, #334155)';
                    }}
                  >
                    {p}
                  </button>
                ))}
              </div>
            </div>
          )}

          
          <div
            style={{
              flex: 1,
              overflowY: 'auto',
              padding: '14px',
              display: 'flex',
              flexDirection: 'column',
              gap: '14px',
              background: 'var(--bg-primary, #090d16)',
            }}
          >
            {messages.map((msg) => {
              const isUser = msg.sender === 'user';
              const isThoughtOpen = expandedThoughtId === msg.id;

              return (
                <div
                  key={msg.id}
                  style={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: isUser ? 'flex-end' : 'flex-start',
                    maxWidth: '100%',
                  }}
                >
                  
                  {!isUser && msg.toolCalls && msg.toolCalls.length > 0 && (
                    <div className="d-flex flex-wrap gap-1 mb-2">
                      {msg.toolCalls.map((t, idx) => (
                        <span
                          key={idx}
                          style={{
                            fontSize: '11px',
                            fontWeight: 600,
                            padding: '3px 9px',
                            borderRadius: '12px',
                            background: 'rgba(99, 102, 241, 0.18)',
                            color: 'var(--primary-accent, #818cf8)',
                            border: '1px solid rgba(129, 140, 248, 0.35)',
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '5px',
                          }}
                        >
                          <span>{t.toolName === 'addToCart' ? '🛒' : t.toolName === 'searchProducts' ? '⚙️' : t.toolName === 'applyCoupon' ? '🏷️' : '📦'}</span>
                          <span>{t.toolName}</span>
                          <span style={{ color: '#10b981' }}>✓</span>
                        </span>
                      ))}
                    </div>
                  )}

                  
                  <div
                    style={{
                      padding: '12px 16px',
                      borderRadius: isUser ? '18px 18px 4px 18px' : '18px 18px 18px 4px',
                      background: isUser ? '#4f46e5' : 'var(--bg-surface, #161f2e)',
                      color: isUser ? '#ffffff' : 'var(--text-primary, #f9fafb)',
                      border: isUser ? 'none' : '1px solid var(--border-color, #2d3748)',
                      fontSize: '13px',
                      lineHeight: '1.55',
                      boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
                      whiteSpace: 'pre-wrap',
                      wordBreak: 'break-word',
                      maxWidth: '88%',
                    }}
                  >
                    {renderMessageContent(msg.message)}
                  </div>

                  
                  {!isUser && msg.thoughtProcess && (
                    <div style={{ marginTop: '6px', maxWidth: '92%' }}>
                      <button
                        onClick={() => setExpandedThoughtId(isThoughtOpen ? null : msg.id)}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: 'var(--primary-accent, #818cf8)',
                          fontSize: '11px',
                          fontWeight: 600,
                          padding: 0,
                          cursor: 'pointer',
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '4px',
                        }}
                      >
                        <span>🧠 {isThoughtOpen ? 'Hide Agent Reasoning' : 'View Agent Reasoning'}</span>
                        <span>{isThoughtOpen ? '▲' : '▼'}</span>
                      </button>

                      {isThoughtOpen && (
                        <div
                          style={{
                            marginTop: '6px',
                            padding: '10px 12px',
                            borderRadius: '8px',
                            background: 'var(--bg-subtle, #1f2937)',
                            border: '1px solid var(--border-color, #374151)',
                            fontSize: '11px',
                            color: 'var(--text-secondary, #cbd5e1)',
                            fontFamily: 'SFMono-Regular, Menlo, Monaco, Consolas, monospace',
                            whiteSpace: 'pre-wrap',
                            lineHeight: '1.45',
                          }}
                        >
                          {msg.thoughtProcess}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}

            {isTyping && (
              <div className="d-flex align-items-center gap-2 p-2" style={{ color: 'var(--primary-accent, #818cf8)', fontSize: '12px' }}>
                <span className="spinner-grow spinner-grow-sm" role="status" aria-hidden="true" />
                <span className="fw-medium">Agent is executing tools & synthesizing response…</span>
              </div>
            )}
          </div>

          
          {suggestedProducts.length > 0 && (
            <div
              style={{
                padding: '10px 14px',
                borderTop: '1px solid var(--border-color, #2d3748)',
                background: 'var(--bg-subtle, #1f2937)',
                flexShrink: 0,
              }}
            >
              <div
                style={{
                  fontSize: '10px',
                  color: 'var(--text-muted, #94a3b8)',
                  marginBottom: '8px',
                  fontWeight: 700,
                  textTransform: 'uppercase',
                  letterSpacing: '0.5px',
                }}
              >
                📦 Grounded Catalog Matches ({suggestedProducts.length})
              </div>
              <div className="d-flex gap-2" style={{ overflowX: 'auto', paddingBottom: '4px' }}>
                {suggestedProducts.slice(0, 4).map((p) => (
                  <div
                    key={p.id}
                    style={{
                      minWidth: '105px',
                      maxWidth: '115px',
                      padding: '8px',
                      borderRadius: '10px',
                      border: '1px solid var(--border-color, #2d3748)',
                      background: 'var(--bg-surface, #161f2e)',
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      textAlign: 'center',
                      position: 'relative',
                    }}
                  >
                    <img
                      src={`${baseUrl}/api/product/${p.id}/image`}
                      alt={p.name}
                      onClick={() => {
                        navigate(`/product/${p.id}`);
                        setIsOpen(false);
                      }}
                      style={{
                        width: '55px',
                        height: '55px',
                        objectFit: 'contain',
                        cursor: 'pointer',
                        borderRadius: '6px',
                        backgroundColor: '#ffffff',
                        padding: '3px',
                      }}
                      onError={(e) => {
                        e.target.style.display = 'none';
                      }}
                    />
                    <div
                      onClick={() => {
                        navigate(`/product/${p.id}`);
                        setIsOpen(false);
                      }}
                      style={{
                        fontSize: '10px',
                        fontWeight: 600,
                        marginTop: '6px',
                        width: '100%',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                        cursor: 'pointer',
                        color: 'var(--text-primary, #f9fafb)',
                      }}
                      title={p.name}
                    >
                      {p.name}
                    </div>
                    <div style={{ fontSize: '11px', color: 'var(--primary-accent, #818cf8)', fontWeight: 700, marginTop: '2px' }}>
                      ₹{p.price?.toLocaleString('en-IN')}
                    </div>
                    <button
                      onClick={() => {
                        addToCart(p);
                        showNotification(`🛒 Added "${p.name}" to cart!`);
                      }}
                      className="btn btn-sm btn-outline-primary w-100 mt-2 py-0 px-1"
                      style={{ fontSize: '10px', fontWeight: 600, borderRadius: '6px' }}
                    >
                      + Add
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          
          <div
            style={{
              padding: '12px 14px',
              borderTop: '1px solid var(--border-color, #2d3748)',
              background: 'var(--bg-surface, #161f2e)',
            }}
          >
            <form
              onSubmit={(e) => {
                e.preventDefault();
                const val = inputRef.current ? inputRef.current.value : '';
                if (val && val.trim()) {
                  handleSend(val.trim());
                  if (inputRef.current) inputRef.current.value = '';
                }
              }}
              className="d-flex align-items-center gap-2"
            >
              <input
                ref={inputRef}
                type="text"
                placeholder="Ask or command the agent (e.g. 'Add Sony headphones')..."
                disabled={isTyping}
                style={{
                  flex: 1,
                  padding: '10px 14px',
                  borderRadius: '24px',
                  border: '1px solid var(--border-color, #334155)',
                  outline: 'none',
                  fontSize: '12px',
                  background: 'var(--bg-subtle, #1f2937)',
                  color: 'var(--text-primary, #f9fafb)',
                }}
              />
              <button
                type="submit"
                disabled={isTyping}
                style={{
                  width: '38px',
                  height: '38px',
                  borderRadius: '50%',
                  background: '#4f46e5',
                  color: '#ffffff',
                  border: 'none',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  cursor: isTyping ? 'not-allowed' : 'pointer',
                  opacity: isTyping ? 0.6 : 1,
                  transition: 'background 0.2s',
                  fontSize: '14px',
                }}
              >
                ➤
              </button>
            </form>
          </div>
        </div>
      )}

      <style>{`
        @keyframes slideUp {
          from { opacity: 0; transform: translateY(20px) scale(0.98); }
          to   { opacity: 1; transform: translateY(0) scale(1); }
        }
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(-4px); }
          to   { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </>
  );
};

export default AiChatWidget;
