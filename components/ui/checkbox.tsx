import * as React from "react";
import * as CheckboxPrimitive from "@radix-ui/react-checkbox";
import { CheckIcon } from "lucide-react";

import { cn } from "@/lib/utils";

function Checkbox({
  className,
  ...props
}: React.ComponentProps<typeof CheckboxPrimitive.Root>) {
  return (
    <CheckboxPrimitive.Root
      data-slot="checkbox"
      className={cn(
        "peer size-4 shrink-0 rounded border border-white/15 bg-white/5 transition-all duration-200 outline-none focus-visible:ring-2 focus-visible:ring-[#E85D04]/35 disabled:cursor-not-allowed disabled:opacity-50 data-[state=checked]:border-[#E85D04] data-[state=checked]:bg-[#E85D04] data-[state=checked]:text-[#0A0A0A]",
        className,
      )}
      {...props}
    >
      <CheckboxPrimitive.Indicator
        data-slot="checkbox-indicator"
        className="flex items-center justify-center text-current"
      >
        <CheckIcon className="size-3.5" />
      </CheckboxPrimitive.Indicator>
    </CheckboxPrimitive.Root>
  );
}

export { Checkbox };
