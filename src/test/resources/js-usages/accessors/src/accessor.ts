import { v4 as uuidv4 } from 'uuid'
import dayjs from 'dayjs'

export class Session {
  accessor id = uuidv4()
  accessor created = dayjs()
}
