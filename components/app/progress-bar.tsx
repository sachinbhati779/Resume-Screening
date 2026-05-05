import { cn } from "@/lib/utils";

type ProgressBarProps = {
  value: number;
  className?: string;
};

export function ProgressBar({ value, className }: ProgressBarProps) {
  const normalized = Math.min(100, Math.max(0, value));

  return (
    <div
      className={cn(
        "h-2 w-full overflow-hidden rounded-full bg-white/10",
        className,
      )}
    >
      <div
        className="h-full rounded-full bg-[#E85D04] shadow-[0_0_18px_rgba(232,93,4,0.42)] transition-all duration-500"
        style={{ width: `${normalized}%` }}
      />
    </div>
  );
}
