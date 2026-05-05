import * as React from "react";

import { cn } from "@/lib/utils";

type GlassPanelProps = React.HTMLAttributes<HTMLDivElement>;

export function GlassPanel({ className, ...props }: GlassPanelProps) {
  return (
    <div
      className={cn(
        "rounded-lg border border-white/10 bg-white/5 shadow-[0_24px_80px_rgba(0,0,0,0.34)] backdrop-blur-xl",
        className,
      )}
      {...props}
    />
  );
}
