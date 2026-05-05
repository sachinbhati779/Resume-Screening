"use client";

import { motion, type HTMLMotionProps } from "framer-motion";

import { cn } from "@/lib/utils";

type MotionPanelProps = HTMLMotionProps<"div"> & {
  delay?: number;
};

export function MotionPanel({
  className,
  delay = 0,
  children,
  ...props
}: MotionPanelProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 14 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.42, delay, ease: [0.22, 1, 0.36, 1] }}
      className={cn(className)}
      {...props}
    >
      {children}
    </motion.div>
  );
}
