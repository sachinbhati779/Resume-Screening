import * as React from "react";

import { cn } from "@/lib/utils";

function Input({ className, type, ...props }: React.ComponentProps<"input">) {
  return (
    <input
      type={type}
      data-slot="input"
      className={cn(
        "h-11 w-full rounded-md border border-white/10 bg-white/5 px-3 text-sm text-[#F5F5F5] outline-none transition-all duration-200 placeholder:text-[#A3A3A3]/70 focus:border-[#E85D04]/60 focus:bg-white/10 focus:ring-2 focus:ring-[#E85D04]/20 disabled:cursor-not-allowed disabled:opacity-50",
        className,
      )}
      {...props}
    />
  );
}

export { Input };
