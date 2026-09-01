// Modal genérico reutilizable para el frontend.
import type { ReactNode } from 'react'

interface ModalProps {
  open: boolean
  title: string
  onClose: () => void
  children: ReactNode
}

export function Modal({ open, title, onClose, children }: ModalProps) {
  if (!open) return null

  return (
    <div className="modal-overlay">
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3 className="mb-0">{title}</h3>
          <button className="modal-close" onClick={onClose} aria-label="Cerrar">
            ×
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}
