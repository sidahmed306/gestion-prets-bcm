import axios from "axios"
import type { ApiErrorBody } from "./types"

export const api = axios.create({
  baseURL: "/api",
  headers: { "Content-Type": "application/json" },
})

export class ApiError extends Error {
  status: number
  fieldErrors?: Record<string, string> | null

  constructor(body: ApiErrorBody) {
    super(body.message)
    this.status = body.status
    this.fieldErrors = body.fieldErrors
  }
}

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.data?.message) {
      return Promise.reject(new ApiError(error.response.data as ApiErrorBody))
    }
    return Promise.reject(error)
  },
)
