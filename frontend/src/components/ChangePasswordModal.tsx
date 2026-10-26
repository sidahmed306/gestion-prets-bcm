import { useState } from "react"
import { useMutation } from "@tanstack/react-query"
import { authApi } from "../api/auth"
import { Modal } from "./Modal"
import { Button } from "./Button"
import { TextField } from "./Form"
import { useToast } from "./Toast"
import { getErrorMessage } from "../lib/errors"

export function ChangePasswordModal({ open, onClose }: { open: boolean; onClose: () => void }) {
  const notify = useToast()
  const [ancien, setAncien] = useState("")
  const [nouveau, setNouveau] = useState("")
  const [confirmation, setConfirmation] = useState("")
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  function fermer() {
    setAncien("")
    setNouveau("")
    setConfirmation("")
    setFieldErrors({})
    onClose()
  }

  const mutation = useMutation({
    mutationFn: () => authApi.changerMotDePasse(ancien, nouveau),
    onSuccess: () => {
      notify("success", "Mot de passe modifié")
      fermer()
    },
    onError: (error) => {
      setFieldErrors((error as any).fieldErrors ?? {})
      notify("error", getErrorMessage(error))
    },
  })

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (nouveau !== confirmation) {
      setFieldErrors({ confirmation: "Les deux mots de passe ne correspondent pas" })
      return
    }
    mutation.mutate()
  }

  return (
    <Modal open={open} onClose={fermer} title="Changer mon mot de passe">
      <form onSubmit={handleSubmit} className="space-y-4">
        <TextField
          label="Mot de passe actuel"
          type="password"
          value={ancien}
          onChange={(e) => setAncien(e.target.value)}
          error={fieldErrors.ancienMotDePasse}
          autoComplete="current-password"
          required
        />
        <TextField
          label="Nouveau mot de passe (8 caractères minimum)"
          type="password"
          value={nouveau}
          onChange={(e) => setNouveau(e.target.value)}
          error={fieldErrors.nouveauMotDePasse}
          autoComplete="new-password"
          minLength={8}
          required
        />
        <TextField
          label="Confirmer le nouveau mot de passe"
          type="password"
          value={confirmation}
          onChange={(e) => setConfirmation(e.target.value)}
          error={fieldErrors.confirmation}
          autoComplete="new-password"
          required
        />
        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="secondary" onClick={fermer}>
            Annuler
          </Button>
          <Button type="submit" disabled={mutation.isPending}>
            {mutation.isPending ? "Enregistrement..." : "Enregistrer"}
          </Button>
        </div>
      </form>
    </Modal>
  )
}
