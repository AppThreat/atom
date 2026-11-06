<script lang="ts">
import { format } from 'date-fns'

export function stamp(d: Date) {
  return format(d, 'yyyy-MM-dd')
}
</script>

<p>{stamp(new Date())}</p>
<small>{format(new Date(), 'HH:mm')}</small>
