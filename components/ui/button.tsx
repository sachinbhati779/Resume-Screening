import * as React from "react";
import { Slot } from "@radix-ui/react-slot";
import { cva, type VariantProps } from "class-variance-authority";

import { cn } from "@/lib/utils";

const buttonVariants = cva(
  "inline-flex h-10 shrink-0 items-center justify-center gap-2 rounded-md px-4 text-sm font-medium transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#E85D04]/45 disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:size-4 [&_svg]:shrink-0",
  {
    variants: {
      variant: {
        default:
          "bg-[#E85D04] text-[#0A0A0A] shadow-[0_0_28px_rgba(232,93,4,0.22)] hover:-translate-y-0.5 hover:shadow-[0_0_34px_rgba(232,93,4,0.32)]",
        secondary:
          "border border-white/10 bg-white/10 text-[#F5F5F5] hover:-translate-y-0.5 hover:bg-white/15",
        outline:
          "border border-white/10 bg-transparent text-[#F5F5F5] hover:-translate-y-0.5 hover:border-[#E85D04]/55 hover:bg-white/5",
        ghost: "bg-transparent text-[#A3A3A3] hover:bg-white/5 hover:text-[#F5F5F5]",
      },
      size: {
        default: "h-10 px-4",
        sm: "h-9 px-3 text-xs",
        lg: "h-12 px-5",
        icon: "size-10 p-0",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  },
);

function Button({
  className,
  variant,
  size,
  asChild = false,
  ...props
}: React.ComponentProps<"button"> &
  VariantProps<typeof buttonVariants> & {
    asChild?: boolean;
  }) {
  const Comp = asChild ? Slot : "button";

  return (
    <Comp
      data-slot="button"
      className={cn(buttonVariants({ variant, size, className }))}
      {...props}
    />
  );
}

export { Button, buttonVariants };
